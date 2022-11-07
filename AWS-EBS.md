# Elastic Beanstalk Deployment

- Setup [AWS-CLI](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html) and [eb-CLI](https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/eb-cli3-install.html) using an [AWS IAM user via your AWS account](https://us-east-1.console.aws.amazon.com/iamv2/home?region=us-east-1#/users)

- Enter into project directory, and initialize and deploy to your AWS account

- Make note of S3 bucket supporting the code deploy, or create in advance

```
eb init
eb setenv SPRING_PROFILES_ACTIVE=beanstalk -e api-keysupport-rest-dev
eb setenv SERVER_PORT=5000 -e api-keysupport-rest-dev
eb setenv S3_BUCKET=elasticbeanstalk-us-east-1-216896468348 -e api-keysupport-rest-dev
eb setenv BASE_URI=https://api.keysupport.org -e api-keysupport-rest-dev
eb use my-env-name
eb list
eb create --single
```

Deploy the configuration directory to the bucket

```
aws s3 sync configuration s3://elasticbeanstalk-us-east-1-216896468348/configuration
```

Deploy the application

```
mvn clean package spring-boot:repackage
eb deploy
eb status
```
- Scale the number of nodes to 2

```
eb scale 2
```

## TLS & Default Deploy Security Considerations

Deploying to Elasic Beanstalk according to the steps above will default to HTTP without TLS.

Rather than altering the Elastic Load Balancer with a custom domain and certificate, I opted to use the AWS API Gateway with a custom domiain.