## Sync DNS Service Deployment

##### Create new user
```shell script
sudo useradd -d /srv/sync-dns -m sync-dns
```

##### Put .jar to `/srv/sync-dns/sync.jar`

##### Credentials `/srv/sync-dns/routers.ini`:
```ini
[Name]
address = 10.0.0.1
username = sync-dns
password = p@ssw0rd
include = Area1,Area2

[Name2]
...
```

##### DNS Records `/srv/sync-dns/records.ini`:
```ini
; will be include automatically for all
[common]
domain = 10.0.0.2
...

[Area1]
...

; will be include automatically for "Name" and override if exists at other areas
[Name]
...
```

##### Service `/usr/lib/systemd/system/sync-dns.service`:
```ini
[Unit]
Description=Sync MikroTik DNS records

[Service]
Type=oneshot
User=sync-dns
WorkingDirectory=~
ExecStart=/usr/bin/java -jar /srv/sync-dns/sync.jar
```

##### Timer `/usr/lib/systemd/system/sync-dns.timer`:
```ini
[Unit]
Description=Daily sync MikroTik DNS records

[Timer]
OnCalendar=*-*-* 2:00:00
Persistent=true

[Install]
WantedBy=timers.target
```

##### Enable and start service
```shell script
sudo systemctl enable sync-dns.timer
sudo systemctl start sync-dns.timer
```