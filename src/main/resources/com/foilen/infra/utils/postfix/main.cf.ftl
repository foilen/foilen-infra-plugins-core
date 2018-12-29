smtpd_banner = $myhostname ESMTP $mail_name (Ubuntu)
biff = no

append_dot_mydomain = no

readme_directory = no

compatibility_level = 2

inet_interfaces = loopback-only
inet_protocols = all

mailbox_size_limit = 0

myhostname = ${hostname}
mynetworks = 127.0.0.0/8 [::ffff:127.0.0.0]/104 [::1]/128
smtpd_relay_restrictions = permit_mynetworks

mydestination = $myhostname
relayhost = [${emailRelay.hostname}]:${emailRelay.port}

smtp_sasl_auth_enable = yes
smtp_sasl_security_options = noanonymous
smtp_sasl_password_maps = hash:/etc/postfix/sasl_passwd
smtp_tls_security_level = encrypt
smtp_tls_CAfile = /etc/ssl/certs/ca-certificates.crt
