# myvim
my vim config

# 使用方式
把 .vimrc copy 到 ~/ 路径下
把 .vim copy 到 ~/ 路径下

ctags
生成c++ tags: ctags -R --c++-kinds=+p --fields=+iaS --extra=+q

为了方便使用在profile里面
alias vctags="ctags -R --c++-kinds=+p --fields=+iaS --extra=+q"

当代码有新的结构，函数定义后，执行以下，就可以了
