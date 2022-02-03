### 인증서 설치를 위한 Certbot tool 설치
Let’s Encrypt SSL 인증서 발급은 Certbot을 이용한다.\
Certbot은 우분투 20.04를 설치 후 letsencrypt을 설치했다면 그 안에 포함되어 있기 때문에 별도 Certbot을 설치할 필요가 없다.
```
sudo apt update
sudo apt-get install  letsencrypt -y
```
### Let’s Encrypt SSL 인증서 발급 방법 4가지

Let’s Encrypt SSL 인증서 발급 방법은 webroot와 Standalone, DNS의 3가지 방식이 있습니다. \
인증서 발급은 사이트에서 인증기관인 Let’s Encrypt에 접속해 이 사이트의 유효성을 검증하는 과정을 거치며 \
이 과정을 아래 3가지 방법 중 하나를 선택해 진행할 수 있다.

#### webroot 
사이트 디렉토리 내에 인증서 유효성을 확인할 수 있는 파일을 업로드하여 인증서를 발급하는 방법
#### 웹서버
  * Nginx나 아파치와 같은 웹서버에서 직접 SSL 인증을 실시하고 웹서버에 맞는 SSL세팅값을 부여
#### Standalone 
사이트 작동을 멈추고 이 사이트의 네크워킹을 이용해 사이트 유효성을 확인해 Let’s Encrypt SSL 인증서를 발급하는 방식
#### DNS 
도메인을 쿼리해 확인되는 TXT 레코드로 사이트 유효성을 확인하는 방법


나는 이중 웹서버를 사용해 인증서를 발급받으려고 한다.

### 웹서버를 통한 Let's Encrypt SSL 인증서
웹서버를 통한 SSL 인증서 발급 방법의 좋은 점은 standalone 방식과 비슷한 방식이면서도 \
다르게 발급 받을 시 사이트 서비스를 중단하지 않아도 된다는 점이고, \
웹서버가 알아서 적절한 SSL 옵션을 제안해 적용해 준다는 점이다.


#### Certot 설치
서버에서 SSL 인증서를 설치할 웹서버용 인증서 설치 툴인 Certbot을 설치한다. 
```php
sudo apt install certbot python3-certbot-nginx
```

#### Nginx 설정 확인
ginx 설정에서 도메인이 제대로 설정되어 있는지 확인합니다. \
다른 방식은 nginx 설정에서 도메인에 적용되어 있는지가 중요하지 않지만 \
웹서버를 이용하는 SSL 인증 방식 선택 시 Nginx 설정에서 도메일이 제대로 설정되어 있어야 한다.



Nginx 설정 파일은 웹서버 설치 방식에 따라 달라지지만\
Nginx 기본 설치 방식으로는 설치 시 /etc/nginx/conf.d/에 있고, \
우분투에서 권장하는 방식으로 설치 시 /etc/nginx/sites-available/ 디렉토리에 있다.


#### 방화벽에서 HTTPS를 허용

아마 기본적으로 설정되어 있기는 하겠지만 80포트와 443포트를 허용해 주고 있는지 확인한다.


우분투 20.04라면 기본 방화벽으로 ufw를 사용하고 있고, \
여기에서라면 ‘Nginx Full’ 옵션을 사용한다.

```
sudo ufw allow ssh
sudo ufw allow 'Nginx Full'
```

#### SSL 인증서 발급
Nginx 웹서버의 경우 아래 명령어를 사용한다.
```
sudo certbot --nginx -d 사이트명
```

그러면 /etc/etsencrypt 폴더에 자동으로 SSL 적용 옵션을 제안해 준다.\
사용자는 이 옵션을 그대로 사용할 수도 있고, 독자적인 옵션을 적용할 수도 있다.



그러나 별도 옵션을 적용하면 nginx가 제안하는 사항들이 제대로 업데이트가 안되기 때문에 \
매번 수작업으로 업데이트를 해주어야 하지만, 독자적인 옵션을 사용하지 않고 \
그냥 웹서버가 제안하는 옵션 그대로 사용한다면 나쁘지 않다고 한다.

