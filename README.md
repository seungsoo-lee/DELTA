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
+ Three virtual machines based on Ubuntu 14.04 LTS 64 bit.
```
- VM-1: Target controller + Application agent
- VM-2: Channel agent
- VM-3: Host agent
```
+ Target Controller ([OpenDaylight_Helium-S3](https://github.com/opendaylight/controller/releases/tag/release%2Fhelium-sr3), [ONOS 1.1.0](https://github.com/opennetworkinglab/onos/tree/onos-1.1) or [Floodlight-0.91](https://github.com/floodlight/floodlight/tree/v0.91)) (in VM-1)
+ [Cbench](https://floodlight.atlassian.net/wiki/display/floodlightcontroller/Cbench) (in VM-2)
+ [Mininet 2.1+](http://mininet.org/download/) (in VM-3)
+ Ant build system
+ Maven build system
+ Vagrant system
+ JDK 1.7+

## Installing DELTA
DELTA installation depends on maven and ant build system. The mvn command is used to install the agent-manager and the sub-agents.

+ STEP 0. Get the source
```
$ git clone https://github.com/OpenNetworkingFoundation/DELTA.git
```

+ STEP 1. Install DELTA dependencies on the host machine.

```
$ cd <DELTA>/tools/dev/
$ ./delta-setup-devenv-ubuntu
```

+ STEP 2. Install 3 virtual machines using vagrant system.

```
$ cd <DELTA>/tools/dev/vagrant
$ vagrant up
```

+ STEP 3. Install DELTA using maven build.

```
$ cd <DELTA>
$ source ./tools/dev/bash_profile
$ mvn clean install
```

+ STEP 4. Install jpcap library for channel agent on VM-2.

```
$ cd DELTA/agents/channel/libs/jpcap/jpcap/0.7
$ scp libjpcap.so vagrant@10.100.100.12:/home/vagrant

$ ssh vagrant@10.100.100.12
vagrant@channel-vm:~$ sudo cp libjpcap.so /usr/lib/
```

+ After installing DELTA, the test environment is automatically setup as below,
![Env](http://143.248.53.145/research/delta/env.png)

## Configuring your own experiments
+ Configure passwd-less ssh login for the VMs.

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

Check if you will be able to access the VMs without having to enter the password.
```

+ The Agent-Manager automatically reads your configuration file and sets up the environment based on the configuration file settings. Setting.cfg contains sample configurations. You can specify your own config file by passing its path:
```
CBENCH_ROOT=/home/vagrant/oflops/cbench/
TARGET_CONTROLLER=Floodlight
OF_PORT=6633
OF_VER=1.0
MITM_NIC=eth1
CONTROLLER_IP=10.100.100.11
SWITCH_IP=10.100.100.13,10.100.100.13,10.100.100.13
```


## Running DELTA
+ STEP 0. Distribute the executable files to VMs

```
$ cd <DELTA>
$ scp ./agents/apps/floodlight/floodlight-0.91/target/floodlight.jar vagrant@10.100.100.11:/home/vagrant
$ scp ./agents/channel/target/delta-agent-channel-1.0-SNAPSHOT-jar-with-dependencies.jar vagrant@10.100.100.12:/home/vagrant
$ scp ./agents/host/target/delta-agent-host-1.0-SNAPSHOT.jar vagrant@10.100.100.13:/home/vagrant
$ scp ./agents/host/test-topo/* vagrant@10.100.100.13:/home/vagrant
```


+ STEP 1. Running Agent Manager first
```
$ cd <DELTA>/manager
$ java -jar target/delta-manager-1.0-SNAPSHOT-jar-with-dependencies.jar ../tools/config/manager.cfg

 DELTA: A Penetration Testing Framework for Software-Defined Networks

 [pP]	- Show all known attacks
 [cC]	- Show configuration info
 [kK]	- Replaying known attack(s)
 [uU]	- Finding an unknown attack
 [qQ]	- Quit

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
