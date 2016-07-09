# DELTA: A Penetration Testing Framework for Software-Defined Networks

## What is DELTA?
DELTA is a penetration testing framework that regenerates known attack scenarios for diverse test cases. This framework also provides the capability of discovering unknown security problems in SDN by employing a fuzzing technique.

+ Agent-Manger is the control tower. It takes full control over all the agents deployed to the target SDN network.
+ Application-Agent is a legitimate SDN application that conducts attack procedures and is controller-dependent. The known malicious functions are implemented as application-agent functions.
+ Channel-Agent is deployed between the controller and the OpenFlow-enabled switch. The agent sniffs and modifies the unencrypted control messages. It is controller-independent.
+ Host-Agent behaves as if it was a legitimate host participating in the target SDN network. The agent demonstrates an attack in which a host attempts to compromise the control plane.

![Delta architecture](http://143.248.53.145/research/delta/arch.png)

## Prerequisites
In order to build and run DELTA the following are required:
+ A host machine based on Ubuntu 14.04 LTS 64 bit (agent manager)
+ Three virtual machines based on Ubuntu 14.04 LTS 64 bit; target controller(s) + application agent (VM1), channel agent (VM2) and host agent (VM3) 
+ Target Controller ([OpenDaylight_Helium-S3](https://github.com/opendaylight/controller/releases/tag/release%2Fhelium-sr3), [ONOS 1.1.0](https://github.com/opennetworkinglab/onos/tree/onos-1.1) or [Floodlight-0.91](https://github.com/floodlight/floodlight/tree/v0.91)) (in VM1)
+ [Cbench](https://floodlight.atlassian.net/wiki/display/floodlightcontroller/Cbench), JPcap library([JPcap 64bit.jar](http://sdnsec.kr/research/delta/jpcap.jar), [libjpcap.so](http://sdnsec.kr/research/delta/libjpcap.so)) (in VM2)
+ [Mininet 2.1+](http://mininet.org/download/) (in VM3)
+ Ant build system
+ Maven build system
+ Vagrant system
+ JDK 1.7+

## Installing DELTA
Delta installation depends on maven and ant build system. The mvn command is used to install the agent-Manager and the sub-agents.

+ STEP 1. Install DELTA dependencies on Ubuntu 14.04 (host machine).

```
$ cd DELTA/tools/dev/
$ ./delta-setup-devenv-ubuntu
```

+ STEP 2. Install 3 virtual machines using vagrant (host machine).

```
$ cd DELTA/tools/dev/vagrant
$ vagrant up
```

+ STEP 3. Configure passwd-less ssh login for target controller(s) (host machine).

```
$ ssh-keygen -t rsa

Generating public/private rsa key pair.
Enter file in which to save the key (/home/sk/.ssh/id_rsa): ## Press Enter
Enter passphrase (empty for no passphrase): ## Enter Passphrase 
Enter same passphrase again: ## Re-enter Passphrase
Your identification has been saved in /home/sk/.ssh/id_rsa.
Your public key has been saved in /home/sk/.ssh/id_rsa.pub.
The key fingerprint is:
e4:6d:fc:7b:6b:d4:0c:04:72:7e:ae:c4:16:f3:13:d1 sk@sk
The key's randomart image is:
+--[ RSA 2048]----+
|          . o... |
|           +  ..E|
|        .   +.o  |
|       o o . *.. |
|        S + + ++ |
|         . + ...o|
|            o.   |
|             .o  |
|            .o.. |
+-----------------+

$ ssh-copy-id -i /home/[name]/.ssh/id_rsa.pub vagrant@10.100.100.11

Now, ssh to your remote as shown here.
$ ssh vagrant@10.100.100.11

Check if you will be able to access the VM1 without having to enter the password.
```

+ STEP 4. Install jpcap library for channel agent (VM2).

```
$ cd DELTA/agents/channel/libs/jpcap/jpcap/0.7
$ scp libjpcap.so vagrant@10.100.100.12:/home/vagrant

$ ssh vagrant@10.100.100.12
vagrant@channel-vm:~$ sudo cp libjpcap.so /usr/lib/
```



## Configuring your own experiments
+ The Agent-Manager automatically reads your configuration file and sets up the environment based on the configuration file settings. Setting.cfg contains sample configurations. You can specify your own config file by passing its path:
```
FLOODLIGHT_ROOT=/home/sdn/floodlight/floodlight-0.91/target/floodlight.jar
FLOODLIGHT_VER=0.91
ODL_ROOT=/home/sdn/odl-helium-sr3/opendaylight/distribution/opendaylight/target/distribution.opendaylight-osgipackage/opendaylight/run.shODL_VER=helium-sr3
ODL_APPAGENT=/home/sdn/odl-helium-sr3/opendaylight/appagent/target/appagent-1.4.5-Helium-SR3.jar
ONOS_ROOT=/home/sdn/onos/onos-1.1.0/
ONOS_VER=1.1.0
ONOS_KARAF_ROOT=/home/sdn/Application/apache-karaf-3.0.4/bin/karaf
CBENCH_ROOT=/home/sdn/oflops/cbench/
TARGET_CONTROLLER=Floodlight
OF_PORT=6633
OF_VER=1.0
MITM_NIC=eth0
CONTROLLER_IP=192.168.100.195
SWITCH_IP=192.168.100.185
```

+ The Channel-Agent automatically reads your configuration file and connects the Agent-Manager.
```
AM_IP=192.168.101.X
AM_PORT=3366
```
+ The Host-Agent automatically reads your configuration file and connects the Agent-Manager.
```
AM_IP=192.168.101.X
AM_PORT=3366
```

## Running DELTA
+ STEP 0. Virtual Machine Setting

> VM 1. Agent-Manager and one of the target controllers are installed.
```
(at least two network interfaces are required)
eth0 192.168.100.X/24 # for controller-switch connection
eth1 192.168.101.X/24 # for Delta agents connection
```

> VM 2. Mininet and Host-Agent are installed.
```
(at least two network interfaces are required)
eth0 192.168.100.X/24 # for controller-switch connection
eth1 192.168.101.X/24 # for Delta agents connection
```


+ STEP 1. Running Agent Manager in VM1
```
$ cd [Delta]/agent-manager
$ sudo java -jar ./target/am.jar ./floodlight.info

 DELTA: A Penetration Testing Framework for Software-Defined Networks

 [pP]	- Show all known attacks
 [cC]	- Show configuration info
 [kK]	- Replaying known attack(s)
 [uU]	- Finding an unknown attack
 [qQ]	- Quit Scanner


Command>_
```

+ STEP 2. Running Channel-Agent
```
$ cd [Delta]/channel-agent
$ sudo java -jar ./target/channel-agent.jar setting.cfg
```

+ STEP 3. Running Host-Agent in VM2
```
$ git clone https://github.com/OpenNetworkingFoundation/DELTA.git
$ cd Delta/host-agent
$ ant

$ sudo python ./topo-setup.py (eth0 ip address in VM1) 6633

mininet> xterm h1

$ (console in h1) cd [Delta]/host-agent
$ (console in h1) java -jar ./target/ha.jar setting.cfg
```

+ STEP 4. Reproducing known attacks in VM1
```
 DELTA: A Penetration Testing Framework for Software-Defined Networks

 [pP]	- Show all known attacks
 [cC]	- Show configuration info
 [kK]	- Replaying known attack(s)
 [uU]	- Finding an unknown attack
 [qQ]	- Quit Scanner


Command> k
Select the attack code (replay all, enter the 'A')> A-2-M-1

 10% ===== |

02:10:46.886 - [A-2-M-1] - Control Message Drop attack start
02:10:46.887 - [A-2-M-1] - Controller setting..
```


## Questions?
Send questions or feedback to: lss365@kaist.ac.kr or chyoon87@kaist.ac.kr

in delta-101
