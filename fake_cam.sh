#!/bin/bash
rm -rf $HOME/.config/chrome-test && /Applications/Google\ Chrome.app/Contents/MacOS/Google\ Chrome --console --no-first-run --user-data-dir=$HOME/.config/chrome-test --use-fake-device-for-media-stream --enable-logging --v=1 --vmodule="*third_party/libjingle/*=3,*=0"
