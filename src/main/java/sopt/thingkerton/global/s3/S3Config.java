package sopt.thingkerton.global.s3;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
@EnableConfigurationProperties(S3Properties.class)
public class S3Config {

    /**
     * 자격 증명은 SDK 기본 체인을 사용한다.
     * EC2에서는 인스턴스 IAM Role, 로컬에서는 ~/.aws/credentials(aws configure)를 사용한다.
     */
    @Bean
    public S3Client s3Client(S3Properties properties) {
        return S3Client.builder()
                .region(Region.of(properties.region()))
                .build();
    }
}
