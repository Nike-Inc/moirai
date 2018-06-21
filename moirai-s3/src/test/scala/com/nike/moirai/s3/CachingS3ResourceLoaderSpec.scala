package com.nike.moirai.s3

import java.io.ByteArrayInputStream

import com.amazonaws.services.s3.model.{GetObjectRequest, ObjectMetadata, S3Object, S3ObjectInputStream}
import com.amazonaws.services.s3.{AmazonS3, Headers}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers}

class CachingS3ResourceLoaderSpec extends FunSpec with Matchers with MockFactory {

  val bucket = "foo.bar.com"
  val objectKey = "folder/file.txt"

  def eTag(content: String): String = {
    content.hashCode.toString
  }

  def mockS3Object(content: String): S3Object = {
    val s3Object = mock[S3Object]

    val mockMetadata = new ObjectMetadata()
    mockMetadata.setHeader(Headers.ETAG, eTag(content))
    (s3Object.getObjectMetadata _).expects().anyNumberOfTimes().returning(mockMetadata)
    val contentStream = new S3ObjectInputStream(new ByteArrayInputStream(content.getBytes), null, false)
    (s3Object.getObjectContent _).expects().returning(contentStream)

    s3Object
  }

  def expectSingleGetObjectRequest(s3Client: AmazonS3)(requestAssertions: GetObjectRequest => Unit)(returnedContent: => Option[String]) = {
    (s3Client.getObject(_: GetObjectRequest)).when(*).once().onCall{ request: GetObjectRequest =>
      requestAssertions(request)
      returnedContent.map(mockS3Object).orNull
    }
  }

  describe("S3ResourceLoader") {
    it("should read the file from the S3 client") {
      val s3Client = stub[AmazonS3]
      val cacher = CachingS3ResourceLoader.withS3Client(s3Client, bucket, objectKey)

      val content =
        """
          |some content
          |on multiple
          |lines
        """.stripMargin

      expectSingleGetObjectRequest(s3Client){ request =>
        request.getBucketName shouldEqual bucket
        request.getKey shouldEqual objectKey
        request.getNonmatchingETagConstraints should contain theSameElementsAs Nil
      }(Some(content))

      cacher.get() shouldEqual content

    }

    describe("after the file has already been read once") {

      val content1 = "content version 1"

      def givenFileReadOnce(s3Client: AmazonS3, cacher: CachingS3ResourceLoader): Unit = {

        expectSingleGetObjectRequest(s3Client) { request =>
          request.getNonmatchingETagConstraints should contain theSameElementsAs Nil
        }(Some(content1))

        cacher.get() shouldEqual content1
      }

      it("should return cached content when S3 does not return new content") {
        val s3Client = stub[AmazonS3]
        val cacher = CachingS3ResourceLoader.withS3Client(s3Client, bucket, objectKey)

        givenFileReadOnce(s3Client, cacher)

        expectSingleGetObjectRequest(s3Client){ request =>()
          request.getNonmatchingETagConstraints should contain theSameElementsAs List(eTag(content1))
        }(None)

        cacher.get() shouldEqual content1
      }

      it("should return new content when S3 returns new content") {
        val s3Client = stub[AmazonS3]
        val cacher = CachingS3ResourceLoader.withS3Client(s3Client, bucket, objectKey)
        givenFileReadOnce(s3Client, cacher)

        val content2 = "content version 2"

        expectSingleGetObjectRequest(s3Client){ request =>
          request.getNonmatchingETagConstraints should contain theSameElementsAs List(eTag(content1))
        }(Some(content2))

        cacher.get() shouldEqual content2
      }

      it("should resume returning cached content after S3 throws an exception") {
        val s3Client = stub[AmazonS3]
        val cacher = CachingS3ResourceLoader.withS3Client(s3Client, bucket, objectKey)
        givenFileReadOnce(s3Client, cacher)

        expectSingleGetObjectRequest(s3Client){ request =>
          request.getNonmatchingETagConstraints should contain theSameElementsAs List(eTag(content1))
        }(throw new RuntimeException("Mock S3 Exception"))

        intercept[RuntimeException](cacher.get())

        expectSingleGetObjectRequest(s3Client){ request =>
          request.getNonmatchingETagConstraints should contain theSameElementsAs List(eTag(content1))
        }(None)

        cacher.get() shouldEqual content1
      }
    }
  }
}
