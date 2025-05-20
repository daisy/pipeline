# AWS Polly Text-To-Speech Adapter

This adapter provides a way to use the Amazon Web Service - Polly Text-to-Speech service in the pipeline. It is based on the [AWS SDK for Java](https://aws.amazon.com/sdk-for-java/) and the [AWS Polly API](https://docs.aws.amazon.com/polly/latest/dg/what-is.html).

## New properties

The following properties are used by this adapter:

- `org.daisy.pipeline.tts.aws.accesskey` (mandatory) : Access key to connect to Amazon Web Service.
- `org.daisy.pipeline.tts.aws.secretkey` (mandatory) : Secret key to connect to Amazon Web Service.
- `org.daisy.pipeline.tts.aws.region` (mandatory) : Region associated with your Amazon Web Service account.
- `org.daisy.pipeline.tts.aws.priority` (defaults to `15`) : priority of usage within the pipeline if other text engine are available

## Usage

This adapter requires authentication keys (namely an access key and a secret key) to access the AWS Polly service.

To get those keys, you will need to create an AWS account and a "IAM user" account, or a root user registered in an instance of an IAM identity center. To create such user account, please refer to the [AWS IAM user creation documentation](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_users_create.html).
Note that while it is not recommended, you can also use your root account to generate the required keys, as explained in [this AWS documention page](https://docs.aws.amazon.com/IAM/latest/UserGuide/id_root-user_manage_add-key.html).

From the IAM User account or your root, you should be able to create access and secret keys by following the instructions in the [AWS IAM user documentation](https://docs.aws.amazon.com/IAM/latest/UserGuide/access-key-self-managed.html).

You should be able to retrieve the region identifier from your associated IAM identity center instance.
As an example for europe/paris, it would be displayed as `eu-west-3`.

## Tests

For unit test to work, please add the required properties mentionned above (`org.daisy.pipeline.tts.aws.{accesskey,secretkey,region}`) to either your configuration or pom, or add the options `-Dorg.daisy.pipeline.tts.aws.accesskey="your_access_key" -Dorg.daisy.pipeline.tts.aws.secretkey="your_secret_key" -Dorg.daisy.pipeline.tts.aws.region="your_region"` to your maven call. 
