## What is a token bucket algorithm?

The token bucket algorithm uses a bucket containing tokens where each token is used to represent the permission to send a unit of data. The bucket is refilled constantly based on a refill rate up to a maximum specified limit. For example, if the refill rate is 10 tokens/second then a token is added to the bucket every 0.1 seconds.
When a unit of data needs to be sent/processed, if there are sufficient number of tokens then the corresponding number of tokens are taken out of the bucket. Otherwise, the data is either enqueued or discarded which is an implementation detail.

## What are the advantages?
1. Easy to understand and implement on your preferred programming language.
2. Allows a burst of activity up to the maximum capacity of the bucket.
3. Prevents excessive consumption of system resources without resorting to fixed timing windows like the fixed-window rate limiting algorithm.

## Where is used?
The applications can be many but the 3 most important ones are:
1. Network traffic to control the flow of data to prevent network congestion.
2. API rate limiting to limit the number of requests a client can make in a give time period.
3. To enable multi-tenant environments to provide efficient access to shared system resources. 

## Steps to implement the token bucket algorithm
1. Create a bucket with a maximum capacity and a refill rate.
2. Refill the bucket with tokens at a constant rate.
3. When a unit of data needs to be sent, check if there are enough tokens in the bucket.
4. If there are enough tokens, take out the corresponding number of tokens from the bucket.
5. If there are not enough tokens, either enqueue the data or discard it.
6. Repeat steps 3-5 until the data is sent.
7. Go back to step 2.
8. Implement the token bucket algorithm in your preferred programming language.
9. Test the implementation with different scenarios to ensure correctness.

## Tech stack used
1. Scala 2.13.14
2. sbt 1.9.9
3. Scalatest 3.2.18
4. Cats IO 3.5.4

## How to run the tests
1. Clone the repository
2. Run the tests with the following command:
```bash
sbt test
```