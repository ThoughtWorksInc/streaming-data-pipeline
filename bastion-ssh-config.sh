#!/usr/bin/env bash
echo "====Updating SSH Config===="

echo "
	User ec2-user
	IdentitiesOnly yes
    StrictHostKeyChecking no
	ForwardAgent yes
	DynamicForward 6789
Host emr-master.twdu1-uat.training
    User hadoop
Host *.twdu1-uat.training
	StrictHostKeyChecking no
	ForwardAgent yes
	ProxyCommand ssh 18.139.38.127 -W %h:%p 2>/dev/null
	User ec2-user
" >> ~/.ssh/config