package sopt.thingkerton.global.s3;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.s3")
public record S3Properties(
        String bucket,
        String region
) {
}
