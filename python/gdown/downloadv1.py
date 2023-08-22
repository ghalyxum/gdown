

import requests


import os.path

import json


import tqdm
import time


import re

from exceptions import FileURLRetrievalError


CHUNK_SIZE = 512 * 1024  # 512KB




def get_url_from_gdrive_confirmation(contents):
    url = ""
    for line in contents.splitlines():
        m = re.search(r'href="(\/uc\?export=download[^"]+)', line)
        if m:
            url = "https://docs.google.com" + m.groups()[0]
            url = url.replace("&amp;", "&")
            break
        m = re.search('id="download-form" action="(.+?)"', line)
        if m:
            url = m.groups()[0]
            url = url.replace("&amp;", "&")
            break
        m = re.search('"downloadUrl":"([^"]+)', line)
        if m:
            url = m.groups()[0]
            url = url.replace("\\u003d", "=")
            url = url.replace("\\u0026", "&")
            break
        m = re.search('<p class="uc-error-subcaption">(.*)</p>', line)
        if m:
            error = m.groups()[0]
            raise FileURLRetrievalError(error)
    if not url:
        raise FileURLRetrievalError(
            "Cannot retrieve the public link of the file. "
            "You may need to change the permission to "
            "'Anyone with the link', or have had many accesses."
        )
    return url





def _get_session(proxy, use_cookies, return_cookies_file=False):
    sess = requests.session()

    sess.headers.update(
        {"User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6)"}
    )

   
    # Load cookies if exists
    cookies_file = "cookies.json"
    if os.path.exists(cookies_file) and use_cookies:
        with open(cookies_file) as f:
            cookies = json.load(f)
        for k, v in cookies:
            sess.cookies[k] = v

    if return_cookies_file:
        return sess, cookies_file
    else:
        return sess




def download(quiet=False,proxy=None,use_cookies=True,verify=True,id=None,speed=None):
    if id is not None:
       url = "https://drive.google.com/uc?id={id}".format(id=id)

    url_origin = url

    ## we didnt use proxy

    sess, cookies_file = _get_session(
        proxy=proxy, use_cookies=use_cookies, return_cookies_file=True
    )

    res = sess.get(url, stream=True, verify=verify)

    if use_cookies:
            
            # Save cookies
            with open(cookies_file, "w") as f:
                cookies = [
                    (k, v)
                    for k, v in sess.cookies.items()
                    if not k.startswith("download_warning_")
                ]
                json.dump(cookies, f, indent=2)


    down_flag = False
    if "Content-Disposition" in res.headers:
            # This is the file
            down_flag = True

    else:
         url = get_url_from_gdrive_confirmation(res.text)
         print(url)
         res = sess.get(url,  stream=True, verify=verify)


    Content_Length = res.headers.get("Content-Length")
    Content_Disposition  = res.headers.get("Content-Disposition")


    print(f"Content-Length : {Content_Length}")
    print(f"Content-Disposition : {Content_Disposition}")




    ## start save response and show download bar
    
    f = open("response.html", "ab")

    try:
        total = res.headers.get("Content-Length")
        if total is not None:
            total = int(total)
        if not quiet:
            pbar = tqdm.tqdm(total=total, unit="B", unit_scale=True)
        t_start = time.time()
        for chunk in res.iter_content(chunk_size=CHUNK_SIZE):
            f.write(chunk)
            if not quiet:
                pbar.update(len(chunk))
            if speed is not None:
                elapsed_time_expected = 1.0 * pbar.n / speed
                elapsed_time = time.time() - t_start
                if elapsed_time < elapsed_time_expected:
                    time.sleep(elapsed_time_expected - elapsed_time)
        if not quiet:
            pbar.close()
            f.close()
           
    finally:
        sess.close()
    


## big   1-11kz8jSB9kBW4wMoCuM7S8Iveg9Jt0r
## link  https://drive.google.com/uc?id=1-11kz8jSB9kBW4wMoCuM7S8Iveg9Jt0r
## simp 1qk3ivTz7bZUNBl-D2ur1_eNG_7b6wm9P
## link  https://drive.google.com/uc?id=1qk3ivTz7bZUNBl-D2ur1_eNG_7b6wm9P

## google ico https://drive.google.com/file/d/1-3di5UZwle99i1H_aquFJpH-GuVBdNQg/view?usp=sharing
## link  https://drive.google.com/uc?id=1-3di5UZwle99i1H_aquFJpH-GuVBdNQg

download(id="1-11kz8jSB9kBW4wMoCuM7S8Iveg9Jt0r")
