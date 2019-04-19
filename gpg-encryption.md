## PGP Key Generation
### Tool
GNU GPG on Linux (GPG is an open source implementation of OpenPGP standard)

### Key Generation Procedure
Generate a key pair using the following command (set options according to requirements: key type, expiration date, name the key, set an email)
~~~
$ gpg2 --gen-key
~~~

Generated keys are stored in GPG keyrings (key stores). You can view them using the following command:
~~~
$ gpg2 --list-keys
 
pub   4096R/C1BB808C 2018-01-17 [expires: 2020-01-17]
uid                  TEST_KEY_6 <TEST_KEY_6@ingenico.com>
sub   4096R/0508F102 2018-01-17 [expires: 2020-01-17]
~~~

PGP Keyrings are located at path: ~/.gnupg
* Secret Keyring: ~/.gnupg/secring.gpg
* Public Keyring: ~/.gnupg/pubring.gpg

Export Public Key (to share it with third parties) using the following command (added the --armor option to export the public key in base64 encoding)
~~~
$ gpg2 --armor --export TEST_KEY_6

-----BEGIN PGP PUBLIC KEY BLOCK-----
Version: GnuPG v2.0.22 (GNU/Linux)
 
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
ysm3ALJ1QMmfHjp3KGYqUg==
=gdcg
-----END PGP PUBLIC KEY BLOCK-----
~~~

Refer to GnuPG documentation for more information on how to manage keys (https://doc.ubuntu-fr.org/gnupg)


## Encryption
Use GnuPG by providing the name of the recipient key to use to encrypt the file (recipient public key). Here we encrypt with our key (for us). Provide the file to encrypt and the output file.

~~~
$ gpg2 --recipient TEST_KEY_6 --output test.txt.gpg --encrypt test.txt
~~~

The file generated using the previous command is in binary format. In order to generate base64 encoded ciphers, the --armor option can be added to the command
~~~
$ gpg2 --armor --recipient TEST_KEY_6 --output test.txt.gpg --encrypt test.txt
~~~
~~~
$ more test.txt.gpg 
-----BEGIN PGP MESSAGE-----
Version: GnuPG v2.0.22 (GNU/Linux)
 
hQIMAyQflOoFCPECARAAtAzy0m6PKo2MTTZbaf5yIAuR1wcF2GERX+bL3E+ezpty
uOZi9vtk4ffb8c8ED/Zgzyq9PtY9F3iZ2oLLlPsvbCymKoGzxmBvmzB4tU0pPAt8
eJIl5w4P0oDMvht6AQpVuDXiPZ/aVAcMHjJvB4ly0xIeqAPpdt8Ix1TclNDV0lEX
22nnXe9nkDoESmtqVLG+mAywoDl2sxgXFqbfoMjYiLRzLjIYEdHM2oEYqRc36tW7
m0YZyUOVM6R3zbc75UFBkzVrP8IT1znSZRYU/CtcsgGvia+gaMnMKBtX49OEBznU
LzPEn2TSvPSlmHo1DvJznUU5Ydy4Buv1RndRPsrU8i7bHcVHIu/E5CcHeh00j2Ey
xgvgfFR8c/0OW2CRKWop74aR3Jetx7xD3RrB+Wu2I6H/rOsmO4C9eYdGOq/EK/Bd
/eWBwDUTvSayKW5JFdAo+fc9MzHYGRI26rYm9ziPWBsrTix56E4eSUrX1QyBGQ0W
zcUt9ZEOMlq/JJdxAef5pD0Tc9ljv9gkzHphDC5/jsvPd5YZCD0XDgorX6ItHZsk
FC17XEos79QrKWexQDxCFsEGv2F5Ed29N+eEeJuHKeXjxWDvUxg7/Rt89NO7Oms6
qCtIDEoM8qeJXxcBKg6yAytglMe2+kNnq/UfT5FUeSM49BcP5KYRfM1xiG91J4jS
RgG1UIt4b171pvxYkVrcyDnlXHqkoFNzFgNMk1+BfapTL0nSFBvB6MxHomRioLKl
sA79ieDb5Nljkm/4xsq9YKKm3KkhZe4=
=kDsr
-----END PGP MESSAGE-----
~~~

## Decryption
Use GnuPG with --decrypt option as below:
~~~
$ gpg2 --output test_decrypted.txt --decrypt test.txt.gpg
 
You need a passphrase to unlock the secret key for
user: "TEST_KEY_6 <TEST_KEY_6@ingenico.com>"
4096-bit RSA key, ID 0508F102, created 2018-01-17 (main key ID C1BB808C)
 
gpg: encrypted with 4096-bit RSA key, ID 0508F102, created 2018-01-17
      "TEST_KEY_6 <TEST_KEY_6@ingenico.com>"
~~~
~~~ 
$ more test_decrypted.txt 
test
~~~