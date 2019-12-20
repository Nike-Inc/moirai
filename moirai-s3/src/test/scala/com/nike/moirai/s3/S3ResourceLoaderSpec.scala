package com.nike.moirai.s3

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers}
import software.amazon.awssdk.core.ResponseBytes
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.{GetObjectRequest, GetObjectResponse}

class S3ResourceLoaderSpec  extends FunSpec with Matchers with MockFactory {
  describe("S3ResourceLoader") {
    val s3Client = mock[S3Client]

    val s3ResourceLoader = S3ResourceLoader.withS3Client(s3Client, "foo.bar.com", "folder/file.txt")

    val content =
      """
        |some content
        |on multiple
        |lines
      """.stripMargin

    it("should read the file from the S3 client") {
      (s3Client.getObjectAsBytes(_: GetObjectRequest))
        .expects(
          GetObjectRequest
            .builder()
            .bucket("foo.bar.com")
            .key("folder/file.txt")
            .build())
        .returning(ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), content.getBytes("UTF-8")))


      s3ResourceLoader.get() shouldBe content
    }
  }
}
