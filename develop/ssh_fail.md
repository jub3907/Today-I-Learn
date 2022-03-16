> **git permission denied (publickey). fatal: Could not read from remote repository. Please make sure you have the correct access rights and the repository exists**

## **문제**

git clone, git pull 등 PC에서 명령어를 쳤는데 위와 같은 에러가 난 적이 있을 것이다. git clone하면서 에러가 났는데 이유는 즉슨, 필자의 경우는 기존과 다른 PC에서 접속했는데 SSH key가 이 PC에 없는 경우 이 에러가 났다.

## **이유**

git은 SSH 또는 http 기반으로 사용을 하게 되는데 SSH key로 접속해서 사용하는 경우는 PC마다 SSH key를 등록해 주어야 한다.

## **해결방법**

1. 터미널창을 열고 ssh key 생성 명령어 입력한다.

```
$ ssh-keygen -t rsa -C "[본인의 Github 계정 이메일주소]"
```

2. Enter 입력. id_rsa 파일의 생성되고 경로는 **C:\Users\[사용자]/.ssh/id_rsa** 이다.

3. 비밀번호 입력을 원하면 비밀번호 입력, 아니면 Enter

4. SSH key가 생성 되었다.

5. Github에 Settings 메뉴로 이동한다.

6. Settings에서 SSH keys를 눌러. New SSH key 버튼 클릭하면 SSH key 값을 입력하는 란이 나온다.

7. .ssh 폴더에 id_rsa.pub 파일을 메모장이나 NotePad로 열어보면 key값이 보일 것이다. 전체 복사해서 아까 SSH key 값 입력하는 곳에 붙여준다.

8. 생성이 완료되었으면 PC에 잘 generate 되었는지 터미널에서 확인해보자.

```
$ ssh -T git@github.com
```

출처 : https://maliceit.tistory.com/51
