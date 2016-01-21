
# Coin Flip Protocol with OpenGL Animation:

This folder contains all necessary files, dependencies and resources to run the OpenGL-supported coin flip animation.

This project is rather seen as a proof-of-concept implementation for a real coin flip client over the network.
It works fine in its actual state, but it is not safe to publish or deploy for real use. For a real deployment
more and better counter-measures against possible attacks need to be thought of and implemented.

This coin flip animation is published under the LIC license.

Have fun trying it out and playing around :)

## Config File:

There is a config file in the same folder as the jars and the executable program, called:
**_coinflip_config.conf_**. This config contains some possible adjustments that can be made to
the execution of the coin flip.

There are two main parts. The first section contains variables regarding the coin flip protocol:

- isServer:   A boolean value to determine, if the coinflip is started as a local server or a client.
- useTLS:     A boolean value to decide, if the coinflip uses TLS over the network.
- useBroker:  A boolean value, if a broker should be used for getting a server IP address and Port number.
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

## System Requirements:

The animation is tested for 64bit unix systems (amd64).
Some packages and programs need to be installed on the system, to get the animation to run:

- OpenJDK, Java version 8
- xorg-dev
- mesa-common-dev

## Running:

To run this animation and the coin-flip protocol, simply call:

./run_coinflip_client.sh

