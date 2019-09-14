find . -name "*.jpg" -type f | xargs -L1 -I{} sh -c "curl localhost:3000/pics -b cookie.txt -F file=@{};type=image/jpeg"
