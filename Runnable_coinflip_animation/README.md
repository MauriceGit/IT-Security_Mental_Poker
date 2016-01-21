
# Coin Flip Protocol with OpenGL Animation:

This folder contains all necessary jar files and resources to run the OpenGL-supported coin flip animation.
The animation is tested for 64bit unix systems (amd64). This implementation is merely seen as a proof-of-concept
for the developed coin flipping protocol and SRA implementation and development. Because of its state as proof-of-concept,
this is just a client implementation, regarding to the broker, right now. It queries the broker at https://52.35.76.130:8443/broker/1.0/players and
always selects the first entry to run the coin flipping against.
As of right now there is no way to actively chose a different server.

It can be run as a server, when called with SERVER as first parameter.

## System Requirements:

Some packages and programs need to be installed on the system, to get the animation to run:

- OpenJDK, Java version 8
- xorg-dev
- mesa-common-dev

## Config File:

There is a config file in the same folder as the jars and the executable program, called:
**_coinflip_config.conf_**. This config contains some possible adjustments that can be made to
the execution of the coin flip.

There are two main parts. The first section contains variables regarding the coin flip protocol:

- isServer:   A boolean value to determine, if the coinflip is started as a local server or a client.
- useTLS:     A boolean value to decide, if the coinflip uses TLS over the network.
- useBroker:  A boolean value, if a broker is used for getting a server IP address and Port number.
- brokerURL:  The complete URL, the broker is reachable at.
- serverIP:   The server IP, if the broker is not used.
- serverPort: The server Port, if the broker is not used. And the port, this server is listening at, if executed as server.
- rootCertificateFile:    Certificate root file name.
- rootCertificatePw:      Certificate root password.
- rootCertificateAlias:   Certificate root Alias.
- serverCertificateFile:  Certificate server file name.
- serverCertificatePw:    Certificate server password.
- serverCertificateAlias: Certificate server Alias.
- clientCertificateFile:  Certificate client file name.
- clientCertificatePw:    Certificate client password.
- clientCertificateAlias: Certificate client Alias.
- serialNumberStartsAt:   A BigInteger, at which the serial number starts.

The second section contains variables regarding the animation:

- headFile:    The filename for the texture for the coin (Head side).
- tailFile:    The filename for the texture for the coin (Tail side).
- winMessage:  The message displayed for the winner.
- loseMessage: The message displayed for the loser.

## Running:

To run this animation and the coin-flip protocol, simply call:

./run_coinflip_client.sh

