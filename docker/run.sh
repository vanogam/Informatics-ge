#!/bin/sh
# TODO ბაზისთვის დალოდების კარგი სკრიფტის დაწერაა საჭირო
#reach=0
#server=db:3306
# shellcheck disable=SC2039
#while [ $reach == 0 ]
#do
#  if ping -c1 -W1 $server; then
#    echo ping -c1 -W1 $server;
#    reach=1
#  else
#    echo 'NO'
#    sleep 1s
#  fi
#done
sleep 10s

java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8787 -jar Informatics.jar