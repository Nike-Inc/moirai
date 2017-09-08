package com.nike.moirai.s3

import java.io.ByteArrayInputStream

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.{S3Object, S3ObjectInputStream}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers}

class S3ResourceLoaderSpec  extends FunSpec with Matchers with MockFactory {
  describe("S3ResourceLoader") {
    val s3Client = mock[AmazonS3]
    val s3Object = mock[S3Object]

    val s3ResourceLoader = S3ResourceLoader.withS3Client(s3Client, "foo.bar.com", "folder/file.txt")

    val content =
      """
        |some content
        |on multiple
        |lines
      """.stripMargin

    it("should read the file from the S3 client") {
      (s3Client.getObject(_: String, _: String)).expects("foo.bar.com", "folder/file.txt").returning(s3Object)
      (s3Object.getObjectContent _).expects().returning(new S3ObjectInputStream(new ByteArrayInputStream(content.getBytes), null, false))


      s3ResourceLoader.get() shouldBe content
    }
  }
}
