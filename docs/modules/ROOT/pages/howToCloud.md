# How to setup an IPT using Amazon EC2 cloud service

## Table of Contents

+ [[Introduction|howToCloud#introduction]]
  + [[What are the benefits?|howToCloud#what-are-the-benefits]]
  + [[What are the limitations?|howToCloud#what-are-the-limitations]]
  + [[How much does it cost?|howToCloud#how-much-does-it-cost]]
  + [[How easy is it to use?|howToCloud#how-easy-is-it-to-use]]
+ [[Instructions|howToCloud#instructions]]

### Introduction

It is possible to install the IPT in a cloud deployment using Amazon EC2 Web Services. EC2 allows you to obtain and boot a new server instance running Tomcat in minutes, on which you can install the IPT normally. 

#### What are the benefits? 

If power cuts or bad Internet connectivity are problems in your area, using EC2 will allow you to keep your IPT online more reliably. 

If your server is within a network that has restrictions against public access, using EC2 is a secure alternative that can save you time and effort configuring a proxy server just to access to your IPT. 

#### What are the limitations? 

If your source data is stored in a database within your network, the database administrator will have to allow external access from your cloud instance. Otherwise, your IPT (in the cloud) may not be able to connect to your database any more.
 
#### How much does it cost?

New customers will get free usage for one year as part of [AWS Free Tier](http://aws.amazon.com/free/). Afterwards, I estimated the monthly cost to be about $15 using the [monthly calculator](http://calculator.s3.amazonaws.com/index.html), however, this will obviously vary depending on how much storage is required. Fortunately Amazon EC2 allows you to pay only for capacity that you are actually using to minimize cost. A screenshot showing my monthly estimate is included below.

<img src='https://github.com/gbif/ipt/wiki/gbif-ipt-docs/ipt2/ec2-calculator.png' />

#### How easy is it to use?

Installation is quite straightforward, but the IPT instance will still require regular administration and backups similar to running the IPT on a normal server. Backups of the IPT data directory could be stored for example on an [Amazon Simple Storage Service (S3)](http://aws.amazon.com/s3/) directory that is both secure and low-cost.

### Instructions

1. Follow the ["Getting Started"](http://docs.aws.amazon.com/AWSEC2/latest/UserGuide/EC2_GetStarted.html) instructions provided by Amazon to launch a new instance and connect to it. 
2. Install the latest version of Tomcat on your instance
3. Install the latest version of the IPT on your instance