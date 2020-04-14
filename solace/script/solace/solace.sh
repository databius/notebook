##### Step 1: Get a PubSub+ Software Event Broker Image
##### ##### Step 2.1: Start Docker.
sudo systemctl start docker
##### ##### Step 2.1: Pull the image:
sudo docker pull solace/solace-pubsub-standard

##### Step 2: Create the 1st VMR (solace1)
########## Step 2.1:
sudo docker run -d -p 18080:8080 -p 15555:55555 -p:180:8008 -p:11883:1883 -p:18000:8000 -p:15672:5672 -p:19000:9000 -p:12222:2222 --shm-size=2g --env username_admin_globalaccesslevel=admin --env username_admin_password=admin --name=solace1 solace/solace-pubsub-standard
##### ##### Step 2.2: Solace CLI management access
sudo docker exec -it solace1 /usr/sw/loads/currentload/bin/cli -A
##### ##### Step 2.3:
home
enable
configure
create username red_user password red_user_password cli
global-access-level read-write
message-vpn default-access-level read-write
end
 
home
enable
configure
create message-vpn red_vpn
authentication
basic
auth-type none
exit
exit
no shutdown
end
 
home
enable
configure
message-spool message-vpn red_vpn
max-spool-usage 255
end
 
home
enable
configure
client-username default message-vpn red_vpn
no shutdown
end
 
home
enable
configure
create client-profile red_profile message-vpn red_vpn
allow-bridge-connections
message-spool
allow-guaranteed-message-send
allow-guaranteed-message-receive
allow-transacted-sessions
allow-guaranteed-endpoint-create
end
 
home
enable
configure
create acl-profile red_acl message-vpn red_vpn
client-connect default-action allow
publish-topic
default-action disallow
exceptions smf list try-* test-*
exit
subscribe-topic
default-action disallow
exceptions smf list try-* test-*
end
 
home
enable
configure
create client-username red_user message-vpn red_vpn
guaranteed-endpoint-permission-override
subscription-manager
client-profile red_profile
acl-profile red_acl
no shutdown
end
 
home
enable
configure
create client-profile bridge_profile message-vpn red_vpn
allow-bridge-connections
message-spool
allow-guaranteed-message-send
allow-guaranteed-message-receive
allow-transacted-sessions
allow-guaranteed-endpoint-create
end
 
home
enable
configure
create client-username bridge_user message-vpn red_vpn
password bridge_user_password
guaranteed-endpoint-permission-override
subscription-manager
client-profile bridge_profile
acl-profile default
no shutdown
end
##### ##### Step 3.4: Solace PubSub+ Manager management access:
##### ##### Open a browser and enter this url: http://localhost:18080/.
##### ##### Log in as user admin with default password admin or  user red_user with password red_user_password.

##### Step 3: Create the 2nd VMR (solace2)
##### ##### Step 3.1:
sudo docker run -d -p 28080:8080 -p 25555:55555 -p:280:8008 -p:21883:1883 -p:28000:8000 -p:25672:5672 -p:29000:9000 -p:22222:2222 --shm-size=2g --env username_admin_globalaccesslevel=admin --env username_admin_password=admin --name=solace2 solace/solace-pubsub-standard
##### ##### Step 3.2: Solace CLI management access
sudo docker exec -it solace2 /usr/sw/loads/currentload/bin/cli -A
##### ##### Step 3.3:
home
enable
configure
create username green_user password green_user_password cli
global-access-level read-write
message-vpn default-access-level read-write
end
 
home
enable
configure
create message-vpn green_vpn
authentication
basic
auth-type none
exit
exit
no shutdown
end
 
home
enable
configure
message-spool message-vpn green_vpn
max-spool-usage 255
end
 
home
enable
configure
client-username default message-vpn green_vpn
no shutdown
end
 
home
enable
configure
create client-profile green_profile message-vpn green_vpn
allow-bridge-connections
message-spool
allow-guaranteed-message-send
allow-guaranteed-message-receive
allow-transacted-sessions
allow-guaranteed-endpoint-create
end
 
home
enable
configure
create acl-profile green_acl message-vpn green_vpn
client-connect default-action allow
publish-topic
default-action disallow
exceptions smf list try-* test-*
exit
subscribe-topic
default-action disallow
exceptions smf list try-* test-*
end
 
home
enable
configure
create client-username green_user message-vpn green_vpn
guaranteed-endpoint-permission-override
subscription-manager
client-profile green_profile
acl-profile green_acl
no shutdown
end
 
home
enable
configure
create client-profile bridge_profile message-vpn green_vpn
allow-bridge-connections
message-spool
allow-guaranteed-message-send
allow-guaranteed-message-receive
allow-transacted-sessions
allow-guaranteed-endpoint-create
end
 
home
enable
configure
create client-username bridge_user message-vpn green_vpn
password bridge_user_password
guaranteed-endpoint-permission-override
subscription-manager
client-profile bridge_profile
acl-profile default
no shutdown
end

##### ##### Step 3.4: Solace PubSub+ Manager management access:
##### ##### Open a browser and enter this url: http://localhost:28080/.
##### ##### Log in as user admin with default password admin or  user green_user with password green_user_password.

##### Step 4: Setup VPN Bridge
sudo docker network inspect bridge
##### ##### Option 1: Setup Uni-Directional Message VPN Bridge with Direct Delivery Mode
##### ##### ##### On solace1:

home
enable
configure
create bridge green-to-red message-vpn red_vpn
remote
create message-vpn green_vpn connect-via 172.17.0.3
no shutdown
exit
authentication basic client-username bridge_user password bridge_user_password
subscription-topic try-green deliver-always
subscription-topic test-green deliver-always
exit
no shutdown
exit

##### ##### Option 2: Setup Uni-Directional Message VPN Bridge with Guaranteed Delivery Mode
##### ##### ##### On solace1:

home
enable
configure
create bridge green-to-red message-vpn red_vpn
remote
create message-vpn green_vpn connect-via 172.17.0.3
message-spool queue green_q_green_to_red
no shutdown
exit
authentication basic client-username bridge_user password bridge_user_password
exit
no shutdown
exit   
On solace2:

home
enable
configure
message-spool message-vpn green_vpn
create queue green_q_green_to_red
owner bridge_user
subscription topic try-green
subscription topic test-green
no shutdown
end
Option 3: Setup Bi-Directional Message VPN Bridge with Direct Delivery Mode

##### ##### ##### On solace1:

home
enable
configure
create bridge red-green message-vpn red_vpn
remote
create message-vpn green_vpn connect-via 172.17.0.3
no shutdown
exit
authentication basic client-username bridge_user password bridge_user_password
subscription-topic try-green deliver-always
subscription-topic test-green deliver-always
exit
no shutdown
exit


##### ##### ##### On solace2:

home
enable
configure
create bridge red-green message-vpn green_vpn
remote
create message-vpn red_vpn connect-via 172.17.0.2
no shutdown
exit
authentication basic client-username bridge_user password bridge_user_password
subscription-topic try-red deliver-always
subscription-topic test-red deliver-always
exit
no shutdown
exit

##### ##### Option 4: Setup Bi-Directional Message VPN Bridge with Guaranteed Delivery Mode
##### ##### ##### On solace1:

home
enable
configure
create bridge red-green message-vpn red_vpn
remote
create message-vpn green_vpn connect-via 172.17.0.3
message-spool queue green_q_red_green
no shutdown
exit
authentication basic client-username bridge_user password bridge_user_password
exit
no shutdown
exit
 
home
enable
configure
message-spool message-vpn red_vpn
create queue red_q_red_green
owner bridge_user
subscription topic try-red
subscription topic test-red
no shutdown
end


##### ##### ##### On solace2:

home
enable
configure
create bridge red-green message-vpn green_vpn
remote
create message-vpn red_vpn connect-via 172.17.0.2
message-spool queue red_q_red_green
no shutdown
exit
authentication basic client-username bridge_user password bridge_user_password
exit
no shutdown
 
exit
home
enable
configure
message-spool message-vpn green_vpn
create queue green_q_red_green
owner bridge_user
subscription topic try-green
subscription topic test-green
no shutdown
end