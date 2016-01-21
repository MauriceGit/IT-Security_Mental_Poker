
*Coin Flip Protocol with OpenGL Animation:*

This folder contains all necessary jar files and resources to run the OpenGL-supported coin flip animation.
The animation is tested for 64bit unix systems (amd64). This implementation is merely seen as a proof-of-concept
for the developed coin flipping protocol and SRA implementation and development. Because of its state as proof-of-concept,
this is just a client implementation, regarding to the broker, right now. It queries the broker at https://52.35.76.130:8443/broker/1.0/players and
always selects the first entry to run the coin flipping against.
As of right now there is no way to actively chose a different server.

It can be run as a server, when called with SERVER as first parameter.

**System Requirements:**

Some packages and programs need to be installed on the system, to get the animation to run:

- OpenJDK, Java version 8
- xorg-dev
- mesa-common-dev

**Config File:**

There is a config file in the same folder as the jars and the executable program, called:
*coinflip_config.conf*. This config contains some possible adjustments that can be made to
the execution of the coin flip.

- isServer: A boolean value to determine, if the coinflip is started as a local server or a client.
- dfdsf
- ddf


**Running:**

To run this animation and the coin-flip protocol, simply call:

./run_coinflip_client.sh

