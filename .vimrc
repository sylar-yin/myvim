set nocompatible 
syntax enable
filetype plugin on
filetype indent on
set autoindent

"colorscheme darkblue
colorscheme desert

set nu
set showcmd
set backspace=eol,start,indent
set magic
set showmatch
set nobackup
set nowb
set noswapfile
set ai
set si
set cindent
set wildmenu

set expandtab
set smarttab
set shiftwidth=4
set tabstop=4

set history=400

set encoding=utf8
set fileencodings=utf8,gb2312,gb18030,ucs-bom,latin1

if &term == "xterm"
    set t_Co=8
    set t_Sb=^[[4%dm
    set t_Sf=^[[3%dm
endif

set laststatus=2
highlight StatusLine cterm=bold ctermfg=yellow ctermbg=blue
set statusline=%F%m%r%h%w\[POS=%l,%v][%p%%]\[%{&fileformat}]\[%{&encoding}]\[%{strftime(\"%Y-%m-%d\ %H:%M:%S\")}] 
"set statusline=\ %<%F[%1*%M%*%n%R%H]%=\ %y\ %0(%{&fileformat}\ %{&encoding}\ %c:%l/%L%)\ 

nmap <F7> :TlistToggle <CR>

let g:SuperTabRetainCompletionType=2
let g:SuperTabDefaultCompletionType="<C-X><C-O>"

set tags+=tags
set tags+=~/.vim/tags/libc.tags
set tags+=~/.vim/tags/cpp.tags
set tags+=~/.vim/tags/glib.tags
set tags+=~/.vim/tags/susv2.tags
set tags+=~/workspace/sylar/tags

set completeopt=longest,menu

let OmniCpp_NamespaceSearch=1
let OmniCpp_GlobalScopeSearch=2
let OmniCpp_ShowAccess=1
let OmniCpp_MayCompleteDot=1
let OmniCpp_MayCompleteArrow=1
let OmniCpp_MayCompleteScope=1
let OmniCpp_DefaultNamespaces=["std","_GLIBCXX_STD"]
