

**Running the Coin Flip Animation:**

This folder contains all necessary jar files and resources to run the OpenGL-supported coin flip animation.
The animation is tested for 64bit unix systems (amd64). This implementation is merely seen as a proof-of-concept
for the developed coin flipping protocol and SRA implementation and development. Because of its state as proof-of-concept,
this is just a client implementation right now. It queries the broker at https://52.35.76.130:8443/broker/1.0/players and
always selects the first entry to run the coin flipping against.
As of right now there is no way to actively chose a different server.

To run this animation and the coin-flip protocol, run:

./run_coinflip_client.sh
