# How to setup an IPT using Amazon EC2 cloud service

I recently installed the IPT in a cloud deployment using Amazon EC2 Web Services. EC2 allowed me to obtain and boot a new server instance running Tomcat in minutes, on which I could install the IPT normally.

## How much does it cost?

New customers will get free usage for one year as part of AWS Free Tier. Afterwards, I estimated the monthly cost to be about $15 using the monthly calculator, however, this will obviously vary depending on how much storage is required. Fortunately Amazon EC2 allows you to pay only for capacity that you are actually using to minimize cost. A screenshot showing my monthly estimate is included below.

## How easy is it to use?

Installation is quite straightforward, but the IPT instance will still require regular administration and backups similar to running the IPT on a normal server. Backups of the IPT data directory could be stored for example on an Amazon Simple Storage Service (S3) directory that is both secure and low-cost.