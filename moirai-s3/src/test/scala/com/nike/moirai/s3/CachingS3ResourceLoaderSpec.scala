package com.nike.moirai.s3

import org.scalamock.handlers.CallHandler1
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers}
import software.amazon.awssdk.core.ResponseBytes
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.{GetObjectRequest, GetObjectResponse, S3Exception}

class CachingS3ResourceLoaderSpec extends FunSpec with Matchers with MockFactory {

  val bucket = "foo.bar.com"
  val objectKey = "folder/file.txt"

  def eTag(content: String): String = {
    content.hashCode.toString
  }

  def mockS3Object(content: String): ResponseBytes[GetObjectResponse] = {
    ResponseBytes.fromByteArray(GetObjectResponse.builder().eTag(eTag(content)).build(), content.getBytes("UTF-8"))
  }

  def expectSingleGetObjectRequest(s3Client: S3Client)(requestAssertions: GetObjectRequest => Unit)(returnedContent: => Option[String]): CallHandler1[GetObjectRequest, ResponseBytes[GetObjectResponse]] = {
    (s3Client.getObjectAsBytes(_: GetObjectRequest)).when(*).once().onCall { request: GetObjectRequest =>
      requestAssertions(request)
      returnedContent.map(mockS3Object).getOrElse(throw S3Exception.builder().statusCode(304).build())
    }
  }

  describe("S3ResourceLoader") {
    it("should read the file from the S3 client") {
      val s3Client = stub[S3Client]
      val cacher = CachingS3ResourceLoader.withS3Client(s3Client, bucket, objectKey)

      val content =
        """
          |some content
          |on multiple
          |lines
        """.stripMargin

      expectSingleGetObjectRequest(s3Client){ request =>
        request.bucket() shouldEqual bucket
        request.key() shouldEqual objectKey
        request.ifNoneMatch() shouldBe null
      }(Some(content))

      cacher.get() shouldEqual content
    }

    describe("after the file has already been read once") {

      val content1 = "content version 1"

      def givenFileReadOnce(s3Client: S3Client, cacher: CachingS3ResourceLoader): Unit = {
        expectSingleGetObjectRequest(s3Client) { request =>
          request.ifNoneMatch() shouldBe null
        }(Some(content1))

        cacher.get() shouldEqual content1
      }

      it("should return cached content when S3 does not return new content") {
        val s3Client = stub[S3Client]
        val cacher = CachingS3ResourceLoader.withS3Client(s3Client, bucket, objectKey)

        givenFileReadOnce(s3Client, cacher)

        expectSingleGetObjectRequest(s3Client){ request =>
          request.ifNoneMatch() shouldBe eTag(content1)
        }(None)

        cacher.get() shouldEqual content1
      }

      it("should return new content when S3 returns new content") {
        val s3Client = stub[S3Client]
        val cacher = CachingS3ResourceLoader.withS3Client(s3Client, bucket, objectKey)
        givenFileReadOnce(s3Client, cacher)

        val content2 = "content version 2"

        expectSingleGetObjectRequest(s3Client){ request =>
          request.ifNoneMatch() shouldBe eTag(content1)
        }(Some(content2))

        cacher.get() shouldEqual content2
      }

      it("should resume returning cached content after S3 throws an exception") {
        val s3Client = stub[S3Client]
        val cacher = CachingS3ResourceLoader.withS3Client(s3Client, bucket, objectKey)
        givenFileReadOnce(s3Client, cacher)

        expectSingleGetObjectRequest(s3Client){ request =>
          request.ifNoneMatch() shouldBe eTag(content1)
        }(throw new RuntimeException("Mock S3 Exception"))

        intercept[RuntimeException](cacher.get())

        expectSingleGetObjectRequest(s3Client){ request =>
          request.ifNoneMatch() shouldBe eTag(content1)
        }(None)

        cacher.get() shouldEqual content1
      }
    }
  }
}
