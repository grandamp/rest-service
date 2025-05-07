# Notes

Loads more CRL's, but hits max heap size

```TEXT
2025-04-18T16:44:22,893 [scheduling-1] INFO  o.k.a.p.c.s.IntermediateCacheSingleton - CMS object contains 144 certificates: https://www.idmanagement.gov/implement/tools/CACertificatesValidatingToFederalCommonPolicyG2.p7b
2025-04-18T16:44:22,905 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/346b0e04f8b2af6525b3efcb5c44392a4c84883f.crl
2025-04-18T16:44:22,909 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/e58919e4f9a2bce84da12954165181c7d5c28e9c.crl
2025-04-18T16:44:23,159 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/335ba56f7a55602b814b2614cc79bf4aba8b32bd.crl
2025-04-18T16:44:23,162 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/189b7a8bd9fd20851e84a26e31c7e3a990d21382.crl
2025-04-18T16:44:23,523 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/0bc04a7e3a6259fe52a077ad6c53c99ff9509ada.crl
2025-04-18T16:44:23,526 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/b5ed2e96044ba918f4f4bd12f1638584975f3e5a.crl
2025-04-18T16:44:23,526 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/2efbc08cab5f57e089f0eafea7643d149027f0c6.crl
2025-04-18T16:44:23,527 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/8b907d21ebbc1a157bc2b04e7e7351c80eb6dcb6.crl
2025-04-18T16:44:24,619 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/540b11b01df957cfb07a2d5252f3b3a78c52f4ac.crl
2025-04-18T16:44:24,619 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/2fec9e660a6765f3237344b129fc3bae9017acc3.crl
2025-04-18T16:44:24,778 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/4d31ad51d64e577e67693325037ec629a5ddbaf3.crl
2025-04-18T16:44:25,689 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/5610f4841b4a51789c4ae34caf49c12c2d34822a.crl
2025-04-18T16:44:25,709 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/56d5901aea95d16fdb2ad10e0efb79750805945e.crl
2025-04-18T16:44:25,712 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/17e64bc81a4bc9a7a670b44c4d5ec8f636d43098.crl
2025-04-18T16:44:26,502 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/09e478564102a46b20da93e845f631e14cc4c4fc.crl
2025-04-18T16:44:26,503 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/62e04838566d6f6b25bebbc38858b1ac9c43c95b.crl
2025-04-18T16:44:27,262 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/85e328cf257aa6018dc56ffec55272aa9ea34c4e.crl
2025-04-18T16:44:27,276 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/fb37dd47413f3d7122607f9f8284024009aaca8b.crl
2025-04-18T16:44:27,277 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/1c21f5e395b1757e06874eb7b0e833b1d88a0b65.crl
2025-04-18T16:44:27,277 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/6e7b20044d11c981e9163590d4cbc25b42d60758.crl
2025-04-18T16:44:27,989 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/fe0117a68a2e7a0adb99ee0f4b9483048adc9191.crl
2025-04-18T16:44:27,992 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/23b84eb14e6d24448b4467a765cfa13b399466dc.crl
2025-04-18T16:44:27,994 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/b9b2d8d91cded37bd58e409832764d9c5cd8fda9.crl
2025-04-18T16:44:27,998 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/44706febfcab74d7344b73e1b8992b40445bfb1f.crl
2025-04-18T16:44:27,999 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/98b6340a1bed049a530a8a0573fa4267cd1066b6.crl
2025-04-18T16:44:28,929 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/c22e258e14f08e58bc2e09afe34812ab60b1861c.crl
2025-04-18T16:44:28,932 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/e376e0a59427e8357df8633cfff3f8565b68172f.crl
2025-04-18T16:44:28,935 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/a014b1ba644ef3f93716dbe54b91c1845572842e.crl
2025-04-18T16:44:28,950 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/9d8e6c06639efc183b502dd33bbbda05dd8ca28a.crl
2025-04-18T16:44:28,966 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/7cc34a5cba1f36ab83517df4e0e50e907f1c1341.crl
2025-04-18T16:44:29,740 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/97696dfd7e2be5a7f262bd75ea961ee00cc0a946.crl
2025-04-18T16:44:29,763 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/a2693f3518ed9e21c0130980f4100c04860c56e8.crl
2025-04-18T16:44:29,881 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/cf94c7db7fdd3390e761412d7010e207ec6ee7aa.crl
2025-04-18T16:44:29,882 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/85b5e798a0b71286f7c2bdd2731735c8718b6c0a.crl
2025-04-18T16:44:29,884 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/f8f98b2f7f90439f8fe68c2cb549b84f928b1674.crl
2025-04-18T16:44:29,884 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/59db0f8231ed848c108e47d88dd58eedcafb310a.crl
2025-04-18T16:44:29,888 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/8d474ad15e45eaee2f515847214f12ebca7aa15f.crl
2025-04-18T16:44:29,908 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/66f92598aecbfbe18c008419d485ff9356ead6a6.crl
2025-04-18T16:44:29,942 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/7405e1561a81014cd753e70f4ea2e65815304911.crl
2025-04-18T16:44:30,044 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/38801f7767e9973533eeb05e1b072608962745da.crl
2025-04-18T16:44:30,589 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/4954914c69443bc4f8022cf4f82d335689759810.crl
2025-04-18T16:44:30,592 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/5e519d99eb82737f86104c5cf8d036c696f42e97.crl
2025-04-18T16:44:31,326 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/69c8537b0521968a793b118f163d0287b9018772.crl
2025-04-18T16:44:31,460 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/fff8ae138b922b799241a3765c2c819e9ac59c78.crl
2025-04-18T16:44:31,461 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/e9ba674340ad41229afefa094388f362b86baf2a.crl
2025-04-18T16:44:31,462 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/9d39ad66b0f2a3b67fe0405992b9517551a2843e.crl
2025-04-18T16:44:31,462 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/83f45f35ebccda5d7b994fc2534418405abdef59.crl
2025-04-18T16:44:31,633 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/344afc53a15b60006684d78b21b90857115a1849.crl
2025-04-18T16:44:32,074 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/af9a21c5b31729aba839953e711575a1f0f51607.crl
2025-04-18T16:44:32,078 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/02a4bc7fdc3443d8eb3c3b9e90d6f757a9186f50.crl
2025-04-18T16:44:32,197 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/49dac3330bc70f7d48a3b71f0896378984ccfeaa.crl
2025-04-18T16:44:32,203 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/c080963f8a8080de420f43e6a40fcba76808c8cd.crl
2025-04-18T16:44:32,350 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/a953be6484834b5d26c6273e2ed18468553cd075.crl
2025-04-18T16:44:32,351 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/da9cb61fff679d47910d26e72966146597e68058.crl
2025-04-18T16:44:33,112 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/1d76056a498f1159badf0beb4db2b70cfa26b1c9.crl
2025-04-18T16:44:33,115 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/3766be845697cec68414fe3b772de791dc6acdf5.crl
2025-04-18T16:44:33,116 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/16cdd6ce7fcf17f97e2185f4b1e72c33ff104509.crl
2025-04-18T16:44:33,185 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/cd9a1c6072c1ebbeaec5abac4990eb4d8ef1dfae.crl
2025-04-18T16:44:33,653 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/771441a65d9526d01dff953b628ceab7b55d3b92.crl
2025-04-18T16:44:33,656 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/12e2abe4336b307f3eea6f3e02d61ebc602bf1f5.crl
2025-04-18T16:44:33,685 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/174bb826ba697aad12505745319e57bb74a5da2f.crl
2025-04-18T16:44:33,685 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/9257e2eb2fb88b486c4ddd07b33c6fa7e53990ce.crl
2025-04-18T16:44:33,686 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/55b46c333fe3601aa7ffc3edb4f7e404da29d063.crl
2025-04-18T16:44:34,943 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/d238ddb5ef4b5957367fbfbf9ca67d0c193105ad.crl
2025-04-18T16:44:34,948 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/6c8a94a277b180721d817a16aaf2dcce66ee45c0.crl
2025-04-18T16:44:34,948 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/96bd7a28f20e838a9040eed2a450d77f75a1965c.crl
2025-04-18T16:44:35,461 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/cdc5e6e3e425179770baaa93e2b9be41a3922be7.crl
2025-04-18T16:44:36,415 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/e8a404029e49d215d1a55fbbeb943904bb82296e.crl
2025-04-18T16:44:36,419 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/2a3751d8b481044fa64014981b8495765ea6bb60.crl
2025-04-18T16:44:36,427 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/9025399a59fc0c7557b9cd6e4c126175d93ae961.crl
2025-04-18T16:44:36,613 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/7509a61513ae873cfa739400f2f0f579b9b27214.crl
2025-04-18T16:44:36,616 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/f3ed39b9da1b4d033c261539d833b508ef383e39.crl
2025-04-18T16:44:36,616 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/08893ace13bc1cf23a2d98310ba9fe3879fd8222.crl
2025-04-18T16:44:37,320 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/ed186c1cf230862f832d515e2e8f446beb8e6c60.crl
2025-04-18T16:44:37,612 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/79f00049eb7f77c25d410265348a90239b1e076f.crl
2025-04-18T16:44:37,616 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/0b7ade179aade67ad55c6c3a7b16f762e4a1901d.crl
2025-04-18T16:44:37,862 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/4b1801bb5afa8aebdef4161e44ea440502a05af3.crl
2025-04-18T16:44:38,498 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/7a8b3c0692dc1ea8d282ac1b746f743d4ed1a89b.crl
2025-04-18T16:44:38,499 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/cd1ec7e1fb18e5c9a8aefa6673655649ff06bb85.crl
2025-04-18T16:44:38,499 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/cc006861a6a50393100a1b61b78718c14556da82.crl
2025-04-18T16:44:38,499 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/8cd6d469a9e485413a6aa65eda511a178d928b6c.crl
2025-04-18T16:44:42,267 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/d7ce9df984d682574316bbcad094f9cef6a77b40.crl
2025-04-18T16:44:42,372 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/8b2307b78e0a20f236f6b96134bfb8f5c36bbee9.crl
2025-04-18T16:44:42,373 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/182e2021b3c957858827e78a7584f373c677e309.crl
2025-04-18T16:44:43,036 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/f4275ca9c37c47f4faa6a7b05997aadd352617e3.crl
2025-04-18T16:44:43,039 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/e6dd1a071acb6bba20b9963993f814dc98033727.crl
2025-04-18T16:44:47,781 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/19c1ce87493380b6f75aac65c374f07f3792a561.crl
2025-04-18T16:44:47,784 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/688415488c54707f2d12580eec1c78ef3c2e5964.crl
2025-04-18T16:44:47,785 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/08e55d81ae79141cbc18a0c10602ff1eaa94bced.crl
2025-04-18T16:44:47,803 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/422ae64ac12c3b0e71689297e0130f760acb947d.crl
2025-04-18T16:44:47,804 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/5719e5d8d6acde78e242f5e445b4d939930bbdda.crl
2025-04-18T16:44:47,804 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/e9217bf2146f3855887aa050099e91721c4bd93b.crl
2025-04-18T16:44:47,933 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/00e9dc6dd24acb90bc490d71098bd983ba61dcf2.crl
2025-04-18T16:44:55,515 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/976248c067692c4c146089d14a003ca39fffa0ae.crl
2025-04-18T16:44:55,519 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/57bcc05edb89ab3bdd47a9721eaecb42196b1e7a.crl
2025-04-18T16:46:53,165 [scheduling-1] ERROR o.s.s.s.TaskUtils$LoggingErrorHandler - Unexpected error occurred in scheduled task
java.lang.OutOfMemoryError: Java heap space
        at java.base/java.lang.reflect.AccessibleObject.slowVerifyAccess(AccessibleObject.java:765)
        at java.base/java.lang.reflect.AccessibleObject.verifyAccess(AccessibleObject.java:741)
        at java.base/java.lang.reflect.AccessibleObject.checkAccess(AccessibleObject.java:713)
        at java.base/java.lang.reflect.Constructor.newInstanceWithCaller(Constructor.java:495)
        at java.base/java.lang.reflect.Constructor.newInstance(Constructor.java:486)
        at java.base/sun.security.x509.CRLExtensions.parseExtension(CRLExtensions.java:123)
        at java.base/sun.security.x509.CRLExtensions.init(CRLExtensions.java:100)
        at java.base/sun.security.x509.CRLExtensions.<init>(CRLExtensions.java:81)
        at java.base/sun.security.x509.X509CRLEntryImpl.parse(X509CRLEntryImpl.java:470)
        at java.base/sun.security.x509.X509CRLEntryImpl.<init>(X509CRLEntryImpl.java:132)
        at java.base/sun.security.x509.X509CRLImpl$TBSCertList.<init>(X509CRLImpl.java:243)
        at java.base/sun.security.x509.X509CRLImpl.parse(X509CRLImpl.java:1164)
        at java.base/sun.security.x509.X509CRLImpl.<init>(X509CRLImpl.java:380)
        at java.base/sun.security.provider.X509Factory.parseX509orPKCS7CRL(X509Factory.java:527)
        at java.base/sun.security.provider.X509Factory.engineGenerateCRLs(X509Factory.java:424)
        at java.base/java.security.cert.CertificateFactory.generateCRLs(CertificateFactory.java:553)
        at org.keysupport.api.pkix.cache.singletons.CRLCacheSingleton.readCRLFromFile(CRLCacheSingleton.java:82)
        at org.keysupport.api.pkix.cache.singletons.CRLCacheSingleton.<init>(CRLCacheSingleton.java:50)
        at org.keysupport.api.pkix.cache.singletons.CRLCacheSingleton$SingletonHelper.<clinit>(CRLCacheSingleton.java:107)
        at org.keysupport.api.pkix.cache.singletons.CRLCacheSingleton.getInstance(CRLCacheSingleton.java:111)
        at org.keysupport.api.config.CacheUpdateConfiguration.refreshIntermediates(CacheUpdateConfiguration.java:41)
        at java.base/java.lang.invoke.DirectMethodHandle$Holder.invokeStatic(DirectMethodHandle$Holder)
        at java.base/java.lang.invoke.LambdaForm$MH/0x00007acd944bc400.invoke(LambdaForm$MH)
        at java.base/java.lang.invoke.Invokers$Holder.invokeExact_MT(Invokers$Holder)
        at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invokeImpl(DirectMethodHandleAccessor.java:153)
        at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103)
        at java.base/java.lang.reflect.Method.invoke(Method.java:580)
        at org.springframework.scheduling.support.ScheduledMethodRunnable.runInternal(ScheduledMethodRunnable.java:130)
        at org.springframework.scheduling.support.ScheduledMethodRunnable.lambda$run$2(ScheduledMethodRunnable.java:124)
        at org.springframework.scheduling.support.ScheduledMethodRunnable$$Lambda/0x00007acd944b85b8.run(Unknown Source)
        at io.micrometer.observation.Observation.observe(Observation.java:498)
        at org.springframework.scheduling.support.ScheduledMethodRunnable.run(ScheduledMethodRunnable.java:124)
```

Updated container entrypoint to increase heap size.  Appears to allow initial load of all CRLs from the inventory into memory

```TEXT
ENTRYPOINT ["/usr/bin/java", "-Xms4096m", "-Xmx10240m", "-jar", "/opt/vss/lib/rest-service-eb.jar"]
```

We need to work on the CRL Cache Singleton, to allow the CRLs to be ingested by the validation process, and to periodically update the CRLs

Below is an example launch of the container, but there is a lot to consider

- The IntermediateCacheSingleton provides a map of intermediate CertStore objects by validation policy
- The new CRLCacheSingleton should provide parity with the Intermediates, or at a minimum, the CRL(s) needed to vaidate a target certificate for a given policy

```TEXT
$ docker run -it --network host --mount source=crldata,destination=/opt/vss/crls vss:latest
22:00:45.229 [main] INFO org.keysupport.api.RestServiceApplication -- Service Starting

2025-04-18T22:00:45,541 [main] INFO  org.keysupport.api.SystemLog -   .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/


2025-04-18T22:00:45,546 [main] INFO  org.keysupport.api.SystemLog -  :: Spring Boot ::                (v3.4.4)


2025-04-18T22:00:45,636 [main] INFO  o.k.api.RestServiceApplication - Starting RestServiceApplication v0.0.1-SNAPSHOT using Java 21.0.7 with PID 1 (/opt/vss/lib/rest-service-eb.jar started by ? in /home/default)
2025-04-18T22:00:45,637 [main] INFO  o.k.api.RestServiceApplication - No active profile set, falling back to 1 default profile: "default"
2025-04-18T22:00:46,595 [main] INFO  o.s.b.w.e.tomcat.TomcatWebServer - Tomcat initialized with port 5000 (http)
2025-04-18T22:00:46,605 [main] INFO  o.a.coyote.http11.Http11NioProtocol - Initializing ProtocolHandler ["http-nio-5000"]
2025-04-18T22:00:46,607 [main] INFO  o.a.catalina.core.StandardService - Starting service [Tomcat]
2025-04-18T22:00:46,607 [main] INFO  o.a.catalina.core.StandardEngine - Starting Servlet engine: [Apache Tomcat/10.1.39]
2025-04-18T22:00:46,633 [main] INFO  o.a.c.c.C.[Tomcat].[localhost].[/] - Initializing Spring embedded WebApplicationContext
2025-04-18T22:00:46,633 [main] INFO  o.s.b.w.s.c.ServletWebServerApplicationContext - Root WebApplicationContext: initialization completed in 941 ms
2025-04-18T22:00:46,856 [main] INFO  o.s.v.b.OptionalValidatorFactoryBean - Failed to set up a Bean Validation provider: jakarta.validation.NoProviderFoundException: Unable to create a Configuration, because no Jakarta Bean Validation provider could be found. Add a provider like Hibernate Validator (RI) to your classpath.
2025-04-18T22:00:47,216 [main] INFO  o.a.coyote.http11.Http11NioProtocol - Starting ProtocolHandler ["http-nio-5000"]
2025-04-18T22:00:47,230 [main] INFO  o.s.b.w.e.tomcat.TomcatWebServer - Tomcat started on port 5000 (http) with context path '/'
2025-04-18T22:00:47,247 [main] INFO  o.k.api.RestServiceApplication - Started RestServiceApplication in 2.008 seconds (process running for 2.476)
2025-04-18T22:00:47,746 [scheduling-1] INFO  o.k.a.singletons.HTTPClientSingleton - {"ResponseHeaders":{":status":["200"],"accept-ranges":["bytes"],"access-control-allow-origin":["*"],"cache-control":["max-age=300"],"content-length":["6192"],"content-security-policy":["default-src 'none'; style-src 'unsafe-inline'; sandbox"],"content-type":["text/plain; charset=utf-8"],"cross-origin-resource-policy":["cross-origin"],"date":["Fri, 18 Apr 2025 22:00:47 GMT"],"etag":["\"e2adfe28520290e316263de1cb5547fd891d23f3dd009b02f9a999f459fad638\""],"expires":["Fri, 18 Apr 2025 22:05:47 GMT"],"source-age":["0"],"strict-transport-security":["max-age=31536000"],"vary":["Authorization,Accept-Encoding,Origin"],"via":["1.1 varnish"],"x-cache":["HIT"],"x-cache-hits":["0"],"x-content-type-options":["nosniff"],"x-fastly-request-id":["bde79283ad9741952c5eecf238f99f9864e69c29"],"x-frame-options":["deny"],"x-github-request-id":["6BB1:215C9E:82F266:A877F5:6802614E"],"x-served-by":["cache-ewr-kewr1740075-EWR"],"x-timer":["S1745013648.612936,VS0,VE57"],"x-xss-protection":["1; mode=block"]}}
2025-04-18T22:00:47,746 [scheduling-1] INFO  o.k.a.s.ValidationPoliciesSingleton - {
    "validationPolicies": [
        {
            "validationPolicyId": "c21f969b-5f03-333d-83e0-4f8f136e7682",
            "validationPolicyName": "default",
            "validationPolicyDescription": "Derived from Default SCVP Policy (1.3.6.1.5.5.7.19.1)",
            "trustAnchors": [
                {
                    "x5t#S256": "X5rswkYWshkTcmAN2A9t0yDIyloM638JyYXr8GlpNPw=",
                    "X509SubjectName": "CN=Federal Common Policy CA G2, OU=FPKI, O=U.S. Government, C=US",
                    "x509Certificate": "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"
                }
            ],
            "userPolicySet": [
                "2.5.29.32.0"
            ],
            "inhibitPolicyMapping": false,
            "requireExplicitPolicy": true,
            "inhibitAnyPolicy": true,
            "cmsIntermediateHintListUri": [
                "https://www.idmanagement.gov/implement/tools/CACertificatesValidatingToFederalCommonPolicyG2.p7b"
            ],
            "excludeIntermediates": []
        },
        {
            "validationPolicyId": "cc54e0ec-49da-333a-8150-2dd00b758b17",
            "validationPolicyName": "aal3",
            "validationPolicyDescription": "Derived from legacy LOA4 validation policy (2.16.840.1.101.10.2.18.2.1.4)",
            "trustAnchors": [
                {
                    "x5t#S256": "X5rswkYWshkTcmAN2A9t0yDIyloM638JyYXr8GlpNPw=",
                    "X509SubjectName": "CN=Federal Common Policy CA G2, OU=FPKI, O=U.S. Government, C=US",
                    "x509Certificate": "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"
                }
            ],
            "userPolicySet": [
                "2.16.840.1.101.3.2.1.3.7",
                "2.16.840.1.101.3.2.1.3.13",
                "2.16.840.1.101.3.2.1.3.16",
                "2.16.840.1.101.3.2.1.3.18",
                "2.16.840.1.101.3.2.1.3.41"
            ],
            "inhibitPolicyMapping": false,
            "requireExplicitPolicy": true,
            "inhibitAnyPolicy": true,
            "cmsIntermediateHintListUri": [
                "https://www.idmanagement.gov/implement/tools/CACertificatesValidatingToFederalCommonPolicyG2.p7b"
            ],
            "excludeIntermediates": []
        }
    ]
}
2025-04-18T22:00:48,756 [scheduling-1] INFO  o.k.a.singletons.HTTPClientSingleton - {"ResponseHeaders":{":status":["200"],"accept-ranges":["bytes"],"access-control-allow-methods":["GET, HEAD"],"cache-control":["max-age=60"],"content-length":["327285"],"content-type":["binary/octet-stream"],"date":["Fri, 18 Apr 2025 22:00:48 GMT"],"etag":["\"a04cc95ad7fb69ed349914918534a955\""],"last-modified":["Thu, 03 Apr 2025 19:28:22 GMT"],"strict-transport-security":["max-age=31536000; preload"],"via":["1.1 578ec28f8e6f7c6503e2a4d2ab7532a2.cloudfront.net (CloudFront)"],"x-amz-cf-id":["8Bq1tHs70Ec-4OqoHWoYxm9u6ZDySIlZytJKDHgyl__UMifaOLyZHw=="],"x-amz-cf-pop":["JFK50-P6"],"x-amz-server-side-encryption":["AES256"],"x-cache":["Miss from cloudfront"],"x-content-type-options":["nosniff"],"x-frame-options":["SAMEORIGIN"],"x-robots-tag":["all"],"x-server":["Cloud.gov Pages"],"x-vcap-request-id":["3df1753e-07ce-4b20-5d14-f418654f50bc"],"x-xss-protection":["1; mode=block"]}}
2025-04-18T22:00:48,797 [scheduling-1] INFO  o.k.a.p.c.s.IntermediateCacheSingleton - CMS object contains 144 certificates: https://www.idmanagement.gov/implement/tools/CACertificatesValidatingToFederalCommonPolicyG2.p7b
2025-04-18T22:00:48,867 [scheduling-1] INFO  o.k.a.singletons.HTTPClientSingleton - {"ResponseHeaders":{":status":["200"],"accept-ranges":["bytes"],"access-control-allow-methods":["GET, HEAD"],"cache-control":["max-age=60"],"content-length":["327285"],"content-type":["binary/octet-stream"],"date":["Fri, 18 Apr 2025 22:00:48 GMT"],"etag":["\"a04cc95ad7fb69ed349914918534a955\""],"last-modified":["Thu, 03 Apr 2025 19:28:22 GMT"],"strict-transport-security":["max-age=31536000; preload"],"via":["1.1 578ec28f8e6f7c6503e2a4d2ab7532a2.cloudfront.net (CloudFront)"],"x-amz-cf-id":["9cdz1tl8C1uhaokET942_KibVQ2T-zBxo33dMEguw-bTfAvsZMtNUg=="],"x-amz-cf-pop":["JFK50-P6"],"x-amz-server-side-encryption":["AES256"],"x-cache":["Hit from cloudfront"],"x-content-type-options":["nosniff"],"x-frame-options":["SAMEORIGIN"],"x-robots-tag":["all"],"x-server":["Cloud.gov Pages"],"x-vcap-request-id":["3df1753e-07ce-4b20-5d14-f418654f50bc"],"x-xss-protection":["1; mode=block"]}}
2025-04-18T22:00:48,880 [scheduling-1] INFO  o.k.a.p.c.s.IntermediateCacheSingleton - CMS object contains 144 certificates: https://www.idmanagement.gov/implement/tools/CACertificatesValidatingToFederalCommonPolicyG2.p7b
2025-04-18T22:00:48,891 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/346b0e04f8b2af6525b3efcb5c44392a4c84883f.crl
2025-04-18T22:00:48,893 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Boeing PCA G3,OU=certservers,O=Boeing,C=US
2025-04-18T22:00:48,893 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Wed Mar 12 18:47:52 UTC 2025
2025-04-18T22:00:48,893 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sat Apr 26 19:07:52 UTC 2025
2025-04-18T22:00:48,893 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/e58919e4f9a2bce84da12954165181c7d5c28e9c.crl
2025-04-18T22:00:49,123 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=HHS-FPKI-Intermediate-CA-E1,OU=Certification Authorities,OU=HHS,O=U.S. Government,C=US
2025-04-18T22:00:49,123 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 20:21:43 UTC 2025
2025-04-18T22:00:49,123 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sun Apr 20 20:21:43 UTC 2025
2025-04-18T22:00:49,123 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/335ba56f7a55602b814b2614cc79bf4aba8b32bd.crl
2025-04-18T22:00:49,123 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=ECA Root CA 4,OU=ECA,O=U.S. Government,C=US
2025-04-18T22:00:49,123 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Tue Apr 01 17:04:37 UTC 2025
2025-04-18T22:00:49,123 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Fri May 02 17:04:37 UTC 2025
2025-04-18T22:00:49,124 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/189b7a8bd9fd20851e84a26e31c7e3a990d21382.crl
2025-04-18T22:00:49,537 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD EMAIL CA-72,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-18T22:00:49,537 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 08:00:00 UTC 2025
2025-04-18T22:00:49,538 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Fri Apr 25 17:00:00 UTC 2025
2025-04-18T22:00:49,538 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/0bc04a7e3a6259fe52a077ad6c53c99ff9509ada.crl
2025-04-18T22:00:49,538 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Entrust Managed PKI Federal Root CA G2,OU=Certification Authorities,O=Entrust,C=US
2025-04-18T22:00:49,538 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Sun Jan 26 20:43:37 UTC 2025
2025-04-18T22:00:49,538 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Mon Jan 26 20:43:37 UTC 2026
2025-04-18T22:00:49,538 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/b5ed2e96044ba918f4f4bd12f1638584975f3e5a.crl
2025-04-18T22:00:49,538 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DigiCert Class 3 SSP Intermediate CA - G4,O=DigiCert\, Inc.,C=US
2025-04-18T22:00:49,538 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Tue Apr 08 01:44:29 UTC 2025
2025-04-18T22:00:49,538 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Tue Apr 08 19:44:29 UTC 2025
2025-04-18T22:00:49,538 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/2efbc08cab5f57e089f0eafea7643d149027f0c6.crl
2025-04-18T22:00:49,538 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Senate PIV-I CA G5 PROD,OU=Office of the Sergeant at Arms,OU=U.S. Senate,O=U.S. Government,C=US
2025-04-18T22:00:49,538 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Tue Apr 08 00:21:14 UTC 2025
2025-04-18T22:00:49,538 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Tue Apr 15 00:21:14 UTC 2025
2025-04-18T22:00:49,538 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/8b907d21ebbc1a157bc2b04e7e7351c80eb6dcb6.crl
2025-04-18T22:00:50,550 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD EMAIL CA-64,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-18T22:00:50,550 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 08:00:00 UTC 2025
2025-04-18T22:00:50,550 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Fri Apr 25 17:00:00 UTC 2025
2025-04-18T22:00:50,550 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/540b11b01df957cfb07a2d5252f3b3a78c52f4ac.crl
2025-04-18T22:00:50,551 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Exostar Federated Identity Service Root CA 2,OU=Certification Authorities,O=Exostar LLC,C=US
2025-04-18T22:00:50,551 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Thu Mar 27 14:16:47 UTC 2025
2025-04-18T22:00:50,551 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Mon Apr 28 14:16:47 UTC 2025
2025-04-18T22:00:50,551 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/2fec9e660a6765f3237344b129fc3bae9017acc3.crl
2025-04-18T22:00:50,661 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=HHS-FPKI-Intermediate-CA-E1,OU=Certification Authorities,OU=HHS,O=U.S. Government,C=US
2025-04-18T22:00:50,661 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 20:21:43 UTC 2025
2025-04-18T22:00:50,661 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sun Apr 20 20:21:43 UTC 2025
2025-04-18T22:00:50,661 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/4d31ad51d64e577e67693325037ec629a5ddbaf3.crl
2025-04-18T22:00:51,623 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD EMAIL CA-63,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-18T22:00:51,623 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 08:00:00 UTC 2025
2025-04-18T22:00:51,623 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Fri Apr 25 17:00:00 UTC 2025
2025-04-18T22:00:51,623 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/5610f4841b4a51789c4ae34caf49c12c2d34822a.crl
2025-04-18T22:00:51,638 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=WidePoint ORC NFI 4,OU=Certification Authorities,O=WidePoint,C=US
2025-04-18T22:00:51,638 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 18:21:59 UTC 2025
2025-04-18T22:00:51,638 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sat Apr 19 18:21:59 UTC 2025
2025-04-18T22:00:51,638 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/56d5901aea95d16fdb2ad10e0efb79750805945e.crl
2025-04-18T22:00:51,640 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=WidePoint ECA 8,OU=Certification Authorities,OU=ECA,O=U.S. Government,C=US
2025-04-18T22:00:51,640 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 15:27:45 UTC 2025
2025-04-18T22:00:51,640 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Fri Apr 25 15:27:45 UTC 2025
2025-04-18T22:00:51,640 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/17e64bc81a4bc9a7a670b44c4d5ec8f636d43098.crl
2025-04-18T22:00:52,397 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD ID CA-63,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-18T22:00:52,397 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 08:00:00 UTC 2025
2025-04-18T22:00:52,397 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Fri Apr 25 17:00:00 UTC 2025
2025-04-18T22:00:52,397 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/09e478564102a46b20da93e845f631e14cc4c4fc.crl
2025-04-18T22:00:52,398 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Carillon Federal Services PIV-I CA2,OU=Certification Authorities,O=Carillon Federal Services Inc.,C=US
2025-04-18T22:00:52,398 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Tue Apr 08 00:00:19 UTC 2025
2025-04-18T22:00:52,398 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Thu Apr 10 00:00:19 UTC 2025
2025-04-18T22:00:52,398 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/62e04838566d6f6b25bebbc38858b1ac9c43c95b.crl
2025-04-18T22:00:53,148 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD ID CA-65,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-18T22:00:53,148 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 08:00:00 UTC 2025
2025-04-18T22:00:53,148 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Fri Apr 25 17:00:00 UTC 2025
2025-04-18T22:00:53,148 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/85e328cf257aa6018dc56ffec55272aa9ea34c4e.crl
2025-04-18T22:00:53,159 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=U.S. Department of State AD High Assurance CA,CN=AIA,CN=Public Key Services,CN=Services,CN=Configuration,DC=state,DC=sbu
2025-04-18T22:00:53,159 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Thu Apr 17 19:18:04 UTC 2025
2025-04-18T22:00:53,159 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Mon Apr 21 19:18:04 UTC 2025
2025-04-18T22:00:53,159 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/fb37dd47413f3d7122607f9f8284024009aaca8b.crl
2025-04-18T22:00:53,159 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Verizon SSP CA A2,OU=SSP,O=Verizon,C=US
2025-04-18T22:00:53,159 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Thu Mar 27 20:21:21 UTC 2025
2025-04-18T22:00:53,159 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Tue Dec 09 08:21:21 UTC 2036
2025-04-18T22:00:53,159 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/1c21f5e395b1757e06874eb7b0e833b1d88a0b65.crl
2025-04-18T22:00:53,160 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=Entrust Managed Services Root CA,OU=Certification Authorities,O=Entrust,C=US
2025-04-18T22:00:53,160 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Tue Apr 08 01:08:22 UTC 2025
2025-04-18T22:00:53,160 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Thu Apr 10 01:08:22 UTC 2025
2025-04-18T22:00:53,160 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/6e7b20044d11c981e9163590d4cbc25b42d60758.crl
2025-04-18T22:00:54,059 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD ID CA-62,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-18T22:00:54,059 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 08:00:00 UTC 2025
2025-04-18T22:00:54,059 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Fri Apr 25 17:00:00 UTC 2025
2025-04-18T22:00:54,059 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/fe0117a68a2e7a0adb99ee0f4b9483048adc9191.crl
2025-04-18T22:00:54,059 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Carillon PKI Services G2 Root CA 2,OU=Certification Authorities,O=Carillon Information Security Inc.,C=CA
2025-04-18T22:00:54,059 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Tue Apr 01 00:00:00 UTC 2025
2025-04-18T22:00:54,059 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Fri May 16 00:00:00 UTC 2025
2025-04-18T22:00:54,059 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/23b84eb14e6d24448b4467a765cfa13b399466dc.crl
2025-04-18T22:00:54,060 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=WidePoint ORC SSP 5,O=ORC PKI,C=US
2025-04-18T22:00:54,060 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 13:46:55 UTC 2025
2025-04-18T22:00:54,060 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sun Apr 20 13:46:54 UTC 2025
2025-04-18T22:00:54,060 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/b9b2d8d91cded37bd58e409832764d9c5cd8fda9.crl
2025-04-18T22:00:54,064 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=IdenTrust ECA S23,OU=Certification Authorities,OU=ECA,O=U.S. Government,C=US
2025-04-18T22:00:54,064 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 19:10:00 UTC 2025
2025-04-18T22:00:54,064 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sat Apr 19 19:10:01 UTC 2025
2025-04-18T22:00:54,064 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/44706febfcab74d7344b73e1b8992b40445bfb1f.crl
2025-04-18T22:00:54,064 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Senate PIV-I CA G6,OU=Office of the Sergeant at Arms,OU=U.S. Senate,O=U.S. Government,C=US
2025-04-18T22:00:54,064 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Sat Apr 12 01:49:51 UTC 2025
2025-04-18T22:00:54,064 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Mon Apr 14 01:49:50 UTC 2025
2025-04-18T22:00:54,064 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/98b6340a1bed049a530a8a0573fa4267cd1066b6.crl
2025-04-18T22:00:54,919 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD EMAIL CA-65,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-18T22:00:54,919 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 08:00:00 UTC 2025
2025-04-18T22:00:54,919 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Fri Apr 25 17:00:00 UTC 2025
2025-04-18T22:00:54,920 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/c22e258e14f08e58bc2e09afe34812ab60b1861c.crl
2025-04-18T22:00:54,920 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Carillon Federal Services PIV-I CA3,OU=Certification Authorities,O=Carillon Federal Services Inc.,C=US
2025-04-18T22:00:54,920 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 12:00:34 UTC 2025
2025-04-18T22:00:54,920 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sun Apr 20 12:00:34 UTC 2025
2025-04-18T22:00:54,920 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/e376e0a59427e8357df8633cfff3f8565b68172f.crl
2025-04-18T22:00:54,922 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Veterans Affairs CA B3,OU=PKI,OU=Services,DC=va,DC=gov
2025-04-18T22:00:54,922 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Mon Mar 17 17:54:58 UTC 2025
2025-04-18T22:00:54,923 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Thu Dec 16 05:54:58 UTC 2027
2025-04-18T22:00:54,923 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/a014b1ba644ef3f93716dbe54b91c1845572842e.crl
2025-04-18T22:00:54,937 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=USPTO_INTR_CA1,CN=AIA,CN=Public Key Services,CN=Services,CN=Configuration,DC=uspto,DC=gov
2025-04-18T22:00:54,937 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 20:31:22 UTC 2025
2025-04-18T22:00:54,937 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sat Apr 26 08:31:22 UTC 2025
2025-04-18T22:00:54,937 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/9d8e6c06639efc183b502dd33bbbda05dd8ca28a.crl
2025-04-18T22:00:54,951 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Leidos FBCA Cloud PKI CA-1,O=Leidos
2025-04-18T22:00:54,951 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 19:50:00 UTC 2025
2025-04-18T22:00:54,951 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sat Apr 19 19:50:01 UTC 2025
2025-04-18T22:00:54,952 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/7cc34a5cba1f36ab83517df4e0e50e907f1c1341.crl
2025-04-18T22:00:55,675 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=DHS CA4,OU=Certification Authorities,OU=Department of Homeland Security,O=U.S. Government,C=US
2025-04-18T22:00:55,675 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 15:35:14 UTC 2025
2025-04-18T22:00:55,675 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sat Apr 19 13:35:14 UTC 2025
2025-04-18T22:00:55,675 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/97696dfd7e2be5a7f262bd75ea961ee00cc0a946.crl
2025-04-18T22:00:55,693 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Raytheon Technologies Medium Assurance CA,OU=Class3-G3,O=CAs,DC=rtx,DC=com
2025-04-18T22:00:55,693 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 15:53:17 UTC 2025
2025-04-18T22:00:55,693 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Fri Apr 25 15:53:17 UTC 2025
2025-04-18T22:00:55,693 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/a2693f3518ed9e21c0130980f4100c04860c56e8.crl
2025-04-18T22:00:55,758 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=U.S. Department of Transportation Agency CA G5,OU=U.S. Department of Transportation,O=U.S. Government,C=US
2025-04-18T22:00:55,758 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 18:35:07 UTC 2025
2025-04-18T22:00:55,758 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Fri Apr 25 18:35:07 UTC 2025
2025-04-18T22:00:55,758 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/cf94c7db7fdd3390e761412d7010e207ec6ee7aa.crl
2025-04-18T22:00:55,758 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=ECA Root CA 5,OU=ECA,O=U.S. Government,C=US
2025-04-18T22:00:55,759 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Tue Apr 01 17:08:12 UTC 2025
2025-04-18T22:00:55,759 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Fri May 02 17:08:12 UTC 2025
2025-04-18T22:00:55,759 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/85b5e798a0b71286f7c2bdd2731735c8718b6c0a.crl
2025-04-18T22:00:55,760 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=WidePoint NFI CA 6,O=ORC PKI,C=US
2025-04-18T22:00:55,760 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Tue Apr 15 07:46:50 UTC 2025
2025-04-18T22:00:55,760 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Thu Apr 17 07:46:49 UTC 2025
2025-04-18T22:00:55,760 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/f8f98b2f7f90439f8fe68c2cb549b84f928b1674.crl
2025-04-18T22:00:55,760 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=IdenTrust Global Common Root CA 1,O=IdenTrust,C=US
2025-04-18T22:00:55,760 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Wed Mar 26 20:48:00 UTC 2025
2025-04-18T22:00:55,760 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Fri Apr 25 20:48:00 UTC 2025
2025-04-18T22:00:55,760 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/59db0f8231ed848c108e47d88dd58eedcafb310a.crl
2025-04-18T22:00:55,762 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Exostar Federated Identity Service Signing CA 4,DC=evincible,DC=com
2025-04-18T22:00:55,763 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 19:20:01 UTC 2025
2025-04-18T22:00:55,763 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sat Apr 19 22:04:01 UTC 2025
2025-04-18T22:00:55,763 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/8d474ad15e45eaee2f515847214f12ebca7aa15f.crl
2025-04-18T22:00:55,777 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=USPTO_INTR_CA1,CN=AIA,CN=Public Key Services,CN=Services,CN=Configuration,DC=uspto,DC=gov
2025-04-18T22:00:55,777 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 20:31:22 UTC 2025
2025-04-18T22:00:55,777 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sat Apr 26 08:31:22 UTC 2025
2025-04-18T22:00:55,777 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/66f92598aecbfbe18c008419d485ff9356ead6a6.crl
2025-04-18T22:00:55,803 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=Entrust NFI Medium Assurance SSP CA,OU=Certification Authorities,O=Entrust,C=US
2025-04-18T22:00:55,803 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 16:14:23 UTC 2025
2025-04-18T22:00:55,803 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sun Apr 20 16:14:23 UTC 2025
2025-04-18T22:00:55,803 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/7405e1561a81014cd753e70f4ea2e65815304911.crl
2025-04-18T22:00:55,879 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=Social Security Administration Certification Authority,OU=SSA,O=U.S. Government,C=US
2025-04-18T22:00:55,879 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 21:02:57 UTC 2025
2025-04-18T22:00:55,879 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sat Apr 19 15:02:57 UTC 2025
2025-04-18T22:00:55,879 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/38801f7767e9973533eeb05e1b072608962745da.crl
2025-04-18T22:00:56,284 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=OCIO CA,OU=Certification Authorities,OU=Department of the Treasury,O=U.S. Government,C=US
2025-04-18T22:00:56,285 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 21:20:38 UTC 2025
2025-04-18T22:00:56,285 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sat Apr 19 15:20:38 UTC 2025
2025-04-18T22:00:56,285 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/4954914c69443bc4f8022cf4f82d335689759810.crl
2025-04-18T22:00:56,285 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=Entrust Managed Services Root CA,OU=Certification Authorities,O=Entrust,C=US
2025-04-18T22:00:56,285 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Tue Apr 08 01:08:21 UTC 2025
2025-04-18T22:00:56,285 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Thu Apr 10 01:08:21 UTC 2025
2025-04-18T22:00:56,285 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/5e519d99eb82737f86104c5cf8d036c696f42e97.crl
2025-04-18T22:00:56,945 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=DHS CA4,OU=Certification Authorities,OU=Department of Homeland Security,O=U.S. Government,C=US
2025-04-18T22:00:56,945 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 15:35:14 UTC 2025
2025-04-18T22:00:56,945 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sat Apr 19 13:35:14 UTC 2025
2025-04-18T22:00:56,945 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/69c8537b0521968a793b118f163d0287b9018772.crl
2025-04-18T22:00:57,114 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=NASA Operational CA,OU=Certification Authorities,OU=NASA,O=U.S. Government,C=US
2025-04-18T22:00:57,114 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 20:09:15 UTC 2025
2025-04-18T22:00:57,114 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sat Apr 19 14:09:15 UTC 2025
2025-04-18T22:00:57,114 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/fff8ae138b922b799241a3765c2c819e9ac59c78.crl
2025-04-18T22:00:57,115 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DoD Interoperability Root CA 2,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-18T22:00:57,115 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Tue Apr 01 17:16:04 UTC 2025
2025-04-18T22:00:57,115 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Fri May 02 17:16:04 UTC 2025
2025-04-18T22:00:57,115 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/e9ba674340ad41229afefa094388f362b86baf2a.crl
2025-04-18T22:00:57,116 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Carillon Federal Services PIV-I CA1,OU=Certification Authorities,O=Carillon Federal Services Inc.,C=US
2025-04-18T22:00:57,117 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 18:00:04 UTC 2025
2025-04-18T22:00:57,117 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sun Apr 20 18:00:04 UTC 2025
2025-04-18T22:00:57,117 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/9d39ad66b0f2a3b67fe0405992b9517551a2843e.crl
2025-04-18T22:00:57,117 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=WidePoint NFI Root 2,OU=Certification Authorities,O=WidePoint,C=US
2025-04-18T22:00:57,117 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Wed Apr 02 14:50:35 UTC 2025
2025-04-18T22:00:57,117 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Fri May 02 14:50:35 UTC 2025
2025-04-18T22:00:57,117 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/83f45f35ebccda5d7b994fc2534418405abdef59.crl
2025-04-18T22:00:57,282 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD ID CA-72,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-18T22:00:57,282 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 08:00:00 UTC 2025
2025-04-18T22:00:57,282 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Fri Apr 25 17:00:00 UTC 2025
2025-04-18T22:00:57,282 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/344afc53a15b60006684d78b21b90857115a1849.crl
2025-04-18T22:00:57,737 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD EMAIL CA-70,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-18T22:00:57,737 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 08:00:00 UTC 2025
2025-04-18T22:00:57,737 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Fri Apr 25 17:00:00 UTC 2025
2025-04-18T22:00:57,737 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/af9a21c5b31729aba839953e711575a1f0f51607.crl
2025-04-18T22:00:57,737 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Veterans Affairs User CA B1,OU=PKI,OU=Services,DC=va,DC=gov
2025-04-18T22:00:57,738 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Mon Mar 17 19:32:22 UTC 2025
2025-04-18T22:00:57,738 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Thu Jan 28 07:32:22 UTC 2027
2025-04-18T22:00:57,738 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/02a4bc7fdc3443d8eb3c3b9e90d6f757a9186f50.crl
2025-04-18T22:00:57,866 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=NASA Operational CA,OU=Certification Authorities,OU=NASA,O=U.S. Government,C=US
2025-04-18T22:00:57,866 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 20:09:15 UTC 2025
2025-04-18T22:00:57,866 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sat Apr 19 14:09:15 UTC 2025
2025-04-18T22:00:57,866 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/49dac3330bc70f7d48a3b71f0896378984ccfeaa.crl
2025-04-18T22:00:57,873 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=NRC SSP Agency CA G4,OU=U.S. Nuclear Regulatory Commission,O=U.S. Government,C=US
2025-04-18T22:00:57,873 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 11:54:43 UTC 2025
2025-04-18T22:00:57,873 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Fri Apr 25 11:54:43 UTC 2025
2025-04-18T22:00:57,873 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/c080963f8a8080de420f43e6a40fcba76808c8cd.crl
2025-04-18T22:00:58,031 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD ID CA-70,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-18T22:00:58,031 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 08:00:00 UTC 2025
2025-04-18T22:00:58,031 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Fri Apr 25 17:00:00 UTC 2025
2025-04-18T22:00:58,031 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/a953be6484834b5d26c6273e2ed18468553cd075.crl
2025-04-18T22:00:58,032 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=Entrust Managed Services Root CA,OU=Certification Authorities,O=Entrust,C=US
2025-04-18T22:00:58,032 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Tue Apr 08 01:08:21 UTC 2025
2025-04-18T22:00:58,032 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Thu Apr 10 01:08:21 UTC 2025
2025-04-18T22:00:58,032 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/da9cb61fff679d47910d26e72966146597e68058.crl
2025-04-18T22:00:58,816 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=Department of Veterans Affairs CA,OU=Certification Authorities,OU=Department of Veterans Affairs,O=U.S. Government,C=US
2025-04-18T22:00:58,816 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 21:31:53 UTC 2025
2025-04-18T22:00:58,816 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sat Apr 19 15:31:53 UTC 2025
2025-04-18T22:00:58,816 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/1d76056a498f1159badf0beb4db2b70cfa26b1c9.crl
2025-04-18T22:00:58,816 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DigiCert Federal SSP Intermediate CA - G6,O=DigiCert\, Inc.,C=US
2025-04-18T22:00:58,816 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Tue Apr 08 00:25:26 UTC 2025
2025-04-18T22:00:58,816 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Tue Apr 08 18:25:26 UTC 2025
2025-04-18T22:00:58,816 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/3766be845697cec68414fe3b772de791dc6acdf5.crl
2025-04-18T22:00:58,817 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=NRC PROD G6 Fed SSP CA,OU=U.S. Nuclear Regulatory Commission,O=U.S. Government,C=US
2025-04-18T22:00:58,817 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Tue Apr 08 00:17:25 UTC 2025
2025-04-18T22:00:58,817 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Wed Apr 09 00:17:25 UTC 2025
2025-04-18T22:00:58,817 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/16cdd6ce7fcf17f97e2185f4b1e72c33ff104509.crl
2025-04-18T22:00:58,923 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=Social Security Administration Certification Authority,OU=SSA,O=U.S. Government,C=US
2025-04-18T22:00:58,923 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 21:02:57 UTC 2025
2025-04-18T22:00:58,923 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sat Apr 19 15:02:57 UTC 2025
2025-04-18T22:00:58,923 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/cd9a1c6072c1ebbeaec5abac4990eb4d8ef1dfae.crl
2025-04-18T22:00:59,485 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=OCIO CA,OU=Certification Authorities,OU=Department of the Treasury,O=U.S. Government,C=US
2025-04-18T22:00:59,485 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 21:20:38 UTC 2025
2025-04-18T22:00:59,485 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sat Apr 19 15:20:38 UTC 2025
2025-04-18T22:00:59,485 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/771441a65d9526d01dff953b628ceab7b55d3b92.crl
2025-04-18T22:00:59,485 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD EMAIL CA-59,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-18T22:00:59,485 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Tue Apr 01 08:30:00 UTC 2025
2025-04-18T22:00:59,485 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Tue Apr 08 17:30:00 UTC 2025
2025-04-18T22:00:59,485 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/12e2abe4336b307f3eea6f3e02d61ebc602bf1f5.crl
2025-04-18T22:00:59,523 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=WidePoint ORC NFI 4,OU=Certification Authorities,O=WidePoint,C=US
2025-04-18T22:00:59,523 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 21:19:33 UTC 2025
2025-04-18T22:00:59,523 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sat Apr 19 21:19:33 UTC 2025
2025-04-18T22:00:59,523 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/174bb826ba697aad12505745319e57bb74a5da2f.crl
2025-04-18T22:00:59,523 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=US Treasury Root CA,OU=Certification Authorities,OU=Department of the Treasury,O=U.S. Government,C=US
2025-04-18T22:00:59,523 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Wed Mar 12 12:56:25 UTC 2025
2025-04-18T22:00:59,523 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Fri Apr 18 03:59:00 UTC 2025
2025-04-18T22:00:59,523 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/9257e2eb2fb88b486c4ddd07b33c6fa7e53990ce.crl
2025-04-18T22:00:59,523 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Lockheed Martin Root Certification Authority 2,OU=Certification Authorities,O=Lockheed Martin Corporation,L=Denver,ST=Colorado,C=US
2025-04-18T22:00:59,523 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Wed Apr 02 15:42:54 UTC 2025
2025-04-18T22:00:59,523 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Fri May 09 16:02:54 UTC 2025
2025-04-18T22:00:59,524 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/55b46c333fe3601aa7ffc3edb4f7e404da29d063.crl
2025-04-18T22:01:00,908 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=Entrust Managed Services SSP CA,OU=Certification Authorities,O=Entrust,C=US
2025-04-18T22:01:00,908 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 09:00:05 UTC 2025
2025-04-18T22:01:00,908 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sun Apr 20 09:00:05 UTC 2025
2025-04-18T22:01:00,908 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/d238ddb5ef4b5957367fbfbf9ca67d0c193105ad.crl
2025-04-18T22:01:00,913 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=FTI Certification Authority,OU=FTI PKI Trust Infrastructure,O=Foundation for Trusted Identity,C=US
2025-04-18T22:01:00,913 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Tue Apr 08 01:00:01 UTC 2025
2025-04-18T22:01:00,913 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Wed Apr 09 01:00:01 UTC 2025
2025-04-18T22:01:00,913 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/6c8a94a277b180721d817a16aaf2dcce66ee45c0.crl
2025-04-18T22:01:00,913 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DoD Root CA 3,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-18T22:01:00,913 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Tue Apr 01 16:52:16 UTC 2025
2025-04-18T22:01:00,913 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Fri May 02 16:52:16 UTC 2025
2025-04-18T22:01:00,913 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/96bd7a28f20e838a9040eed2a450d77f75a1965c.crl
2025-04-18T22:01:01,453 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Entrust Derived Credential SSP CA,OU=Certification Authorities,O=Entrust,C=US
2025-04-18T22:01:01,453 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 17:14:34 UTC 2025
2025-04-18T22:01:01,453 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sun Apr 20 17:14:34 UTC 2025
2025-04-18T22:01:01,453 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/cdc5e6e3e425179770baaa93e2b9be41a3922be7.crl
2025-04-18T22:01:02,381 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD EMAIL CA-62,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-18T22:01:02,381 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 08:00:00 UTC 2025
2025-04-18T22:01:02,381 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Fri Apr 25 17:00:00 UTC 2025
2025-04-18T22:01:02,382 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/e8a404029e49d215d1a55fbbeb943904bb82296e.crl
2025-04-18T22:01:02,382 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=IdenTrust ECA S22,OU=Certification Authorities,OU=ECA,O=U.S. Government,C=US
2025-04-18T22:01:02,382 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 09:10:00 UTC 2025
2025-04-18T22:01:02,382 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sat Apr 19 09:10:00 UTC 2025
2025-04-18T22:01:02,382 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/2a3751d8b481044fa64014981b8495765ea6bb60.crl
2025-04-18T22:01:02,385 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Lockheed Martin Certification Authority 6 G3,OU=Certification Authorities,O=Lockheed Martin Corporation,C=US
2025-04-18T22:01:02,385 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Thu Apr 17 20:57:29 UTC 2025
2025-04-18T22:01:02,385 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sat Apr 19 09:17:29 UTC 2025
2025-04-18T22:01:02,385 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/9025399a59fc0c7557b9cd6e4c126175d93ae961.crl
2025-04-18T22:01:02,612 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD ID CA-71,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-18T22:01:02,613 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 08:00:00 UTC 2025
2025-04-18T22:01:02,613 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Fri Apr 25 17:00:00 UTC 2025
2025-04-18T22:01:02,613 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/7509a61513ae873cfa739400f2f0f579b9b27214.crl
2025-04-18T22:01:02,613 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD ID CA-59,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-18T22:01:02,613 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Tue Apr 01 08:00:00 UTC 2025
2025-04-18T22:01:02,613 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Tue Apr 08 17:00:00 UTC 2025
2025-04-18T22:01:02,613 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/f3ed39b9da1b4d033c261539d833b508ef383e39.crl
2025-04-18T22:01:02,613 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=Entrust Managed Services NFI Root CA,OU=Certification Authorities,O=Entrust,C=US
2025-04-18T22:01:02,613 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Tue Apr 08 01:02:49 UTC 2025
2025-04-18T22:01:02,613 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Thu Apr 10 01:02:49 UTC 2025
2025-04-18T22:01:02,613 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/08893ace13bc1cf23a2d98310ba9fe3879fd8222.crl
2025-04-18T22:01:02,999 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD DERILITY CA-1,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-18T22:01:02,999 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 08:00:00 UTC 2025
2025-04-18T22:01:02,999 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Fri Apr 25 17:00:00 UTC 2025
2025-04-18T22:01:02,999 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/ed186c1cf230862f832d515e2e8f446beb8e6c60.crl
2025-04-18T22:01:03,397 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD EMAIL CA-71,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-18T22:01:03,397 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 08:00:00 UTC 2025
2025-04-18T22:01:03,397 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Fri Apr 25 17:00:00 UTC 2025
2025-04-18T22:01:03,397 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/79f00049eb7f77c25d410265348a90239b1e076f.crl
2025-04-18T22:01:03,397 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Federal Bridge CA G4,OU=FPKI,O=U.S. Government,C=US
2025-04-18T22:01:03,397 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Wed Apr 02 13:19:22 UTC 2025
2025-04-18T22:01:03,397 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sun May 04 13:19:22 UTC 2025
2025-04-18T22:01:03,397 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/0b7ade179aade67ad55c6c3a7b16f762e4a1901d.crl
2025-04-18T22:01:03,466 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD DERILITY CA-3,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-18T22:01:03,466 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 08:00:00 UTC 2025
2025-04-18T22:01:03,466 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Fri Apr 25 17:00:00 UTC 2025
2025-04-18T22:01:03,466 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/4b1801bb5afa8aebdef4161e44ea440502a05af3.crl
2025-04-18T22:01:04,141 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Entrust Derived Credential SSP CA,OU=Certification Authorities,O=Entrust,C=US
2025-04-18T22:01:04,141 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 17:14:34 UTC 2025
2025-04-18T22:01:04,141 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sun Apr 20 17:14:34 UTC 2025
2025-04-18T22:01:04,141 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/7a8b3c0692dc1ea8d282ac1b746f743d4ed1a89b.crl
2025-04-18T22:01:04,142 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=CertiPath Bridge CA - G3,OU=Certification Authorities,O=CertiPath,C=US
2025-04-18T22:01:04,142 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Sun Mar 23 00:00:00 UTC 2025
2025-04-18T22:01:04,142 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Mon Apr 21 23:59:59 UTC 2025
2025-04-18T22:01:04,142 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/cd1ec7e1fb18e5c9a8aefa6673655649ff06bb85.crl
2025-04-18T22:01:04,142 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD DERILITY CA-4,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-18T22:01:04,142 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Mon Apr 07 08:00:00 UTC 2025
2025-04-18T22:01:04,142 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Mon Apr 14 17:00:00 UTC 2025
2025-04-18T22:01:04,142 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/cc006861a6a50393100a1b61b78718c14556da82.crl
2025-04-18T22:01:04,142 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=U.S. Department of State AD Root CA,CN=AIA,CN=Public Key Services,CN=Services,CN=Configuration,DC=state,DC=sbu
2025-04-18T22:01:04,142 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Mon Mar 17 15:10:58 UTC 2025
2025-04-18T22:01:04,142 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Thu Apr 17 05:59:00 UTC 2025
2025-04-18T22:01:04,142 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/8cd6d469a9e485413a6aa65eda511a178d928b6c.crl
2025-04-18T22:01:04,801 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=U.S. Department of State PIV CA2,OU=Certification Authorities,OU=PIV,OU=Department of State,O=U.S. Government,C=US
2025-04-18T22:01:04,801 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Thu Apr 17 13:38:41 UTC 2025
2025-04-18T22:01:04,801 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Fri Apr 25 01:38:41 UTC 2025
2025-04-18T22:01:04,801 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/d7ce9df984d682574316bbcad094f9cef6a77b40.crl
2025-04-18T22:01:04,916 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=Social Security Administration Certification Authority,OU=SSA,O=U.S. Government,C=US
2025-04-18T22:01:04,917 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 21:02:57 UTC 2025
2025-04-18T22:01:04,917 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sat Apr 19 15:02:57 UTC 2025
2025-04-18T22:01:04,917 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/8b2307b78e0a20f236f6b96134bfb8f5c36bbee9.crl
2025-04-18T22:01:04,917 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=WidePoint SSP Intermediate CA,O=ORC PKI,C=US
2025-04-18T22:01:04,917 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Tue Apr 08 01:46:43 UTC 2025
2025-04-18T22:01:04,917 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Thu Apr 10 01:46:42 UTC 2025
2025-04-18T22:01:04,917 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/182e2021b3c957858827e78a7584f373c677e309.crl
2025-04-18T22:01:05,605 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=DHS CA4,OU=Certification Authorities,OU=Department of Homeland Security,O=U.S. Government,C=US
2025-04-18T22:01:05,605 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 15:35:15 UTC 2025
2025-04-18T22:01:05,605 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sat Apr 19 13:35:15 UTC 2025
2025-04-18T22:01:05,605 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/f4275ca9c37c47f4faa6a7b05997aadd352617e3.crl
2025-04-18T22:01:05,605 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Federal Common Policy CA G2,OU=FPKI,O=U.S. Government,C=US
2025-04-18T22:01:05,605 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Wed Apr 02 13:17:51 UTC 2025
2025-04-18T22:01:05,605 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sun May 04 13:17:51 UTC 2025
2025-04-18T22:01:05,605 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/e6dd1a071acb6bba20b9963993f814dc98033727.crl
2025-04-18T22:01:06,856 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=Entrust Managed Services SSP CA,OU=Certification Authorities,O=Entrust,C=US
2025-04-18T22:01:06,857 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 09:00:06 UTC 2025
2025-04-18T22:01:06,857 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sun Apr 20 09:00:06 UTC 2025
2025-04-18T22:01:06,857 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/19c1ce87493380b6f75aac65c374f07f3792a561.crl
2025-04-18T22:01:06,857 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=STRAC Bridge Root Certification Authority,OU=STRAC PKI Trust Infrastructure,O=STRAC,C=US
2025-04-18T22:01:06,857 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Tue Apr 08 01:00:01 UTC 2025
2025-04-18T22:01:06,857 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Wed Apr 09 01:00:01 UTC 2025
2025-04-18T22:01:06,857 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/688415488c54707f2d12580eec1c78ef3c2e5964.crl
2025-04-18T22:01:06,857 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=US Treasury Root CA,OU=Certification Authorities,OU=Department of the Treasury,O=U.S. Government,C=US
2025-04-18T22:01:06,857 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Wed Mar 12 12:56:25 UTC 2025
2025-04-18T22:01:06,857 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Fri Apr 18 03:59:00 UTC 2025
2025-04-18T22:01:06,857 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/08e55d81ae79141cbc18a0c10602ff1eaa94bced.crl
2025-04-18T22:01:06,875 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=IGC CA 1,OU=IdenTrust Global Common,O=IdenTrust,C=US
2025-04-18T22:01:06,875 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 20:30:00 UTC 2025
2025-04-18T22:01:06,875 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sat Apr 19 20:30:01 UTC 2025
2025-04-18T22:01:06,875 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/422ae64ac12c3b0e71689297e0130f760acb947d.crl
2025-04-18T22:01:06,876 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOE SSP CA,OU=Certification Authorities,OU=Department of Energy,O=U.S. Government,C=US
2025-04-18T22:01:06,876 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Tue Apr 08 01:20:33 UTC 2025
2025-04-18T22:01:06,876 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Thu Apr 10 01:20:33 UTC 2025
2025-04-18T22:01:06,876 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/5719e5d8d6acde78e242f5e445b4d939930bbdda.crl
2025-04-18T22:01:06,876 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DigiCert Federal SSP Intermediate CA - G5,O=DigiCert\, Inc.,C=US
2025-04-18T22:01:06,876 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Tue Apr 08 00:25:26 UTC 2025
2025-04-18T22:01:06,876 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Tue Apr 08 18:25:26 UTC 2025
2025-04-18T22:01:06,876 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/e9217bf2146f3855887aa050099e91721c4bd93b.crl
2025-04-18T22:01:07,231 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD ID CA-73,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-18T22:01:07,231 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 08:00:00 UTC 2025
2025-04-18T22:01:07,231 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Fri Apr 25 17:00:00 UTC 2025
2025-04-18T22:01:07,231 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/00e9dc6dd24acb90bc490d71098bd983ba61dcf2.crl
2025-04-18T22:01:08,111 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=Department of Veterans Affairs CA,OU=Certification Authorities,OU=Department of Veterans Affairs,O=U.S. Government,C=US
2025-04-18T22:01:08,111 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 21:31:53 UTC 2025
2025-04-18T22:01:08,111 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sat Apr 19 15:31:53 UTC 2025
2025-04-18T22:01:08,111 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/976248c067692c4c146089d14a003ca39fffa0ae.crl
2025-04-18T22:01:08,112 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=IdenTrust ECA S22C,OU=Certification Authorities,OU=ECA,O=U.S. Government,C=US
2025-04-18T22:01:08,112 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Sat Apr 12 21:50:00 UTC 2025
2025-04-18T22:01:08,112 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sun Apr 13 21:50:00 UTC 2025
2025-04-18T22:01:08,112 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/57bcc05edb89ab3bdd47a9721eaecb42196b1e7a.crl
2025-04-18T22:01:08,961 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD ID CA-64,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-18T22:01:08,961 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 08:00:00 UTC 2025
2025-04-18T22:01:08,961 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Fri Apr 25 17:00:00 UTC 2025
2025-04-18T22:01:08,961 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/5dbd71271d66c286dd05ba17490b4d83903e989e.crl
2025-04-18T22:01:08,961 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Lockheed Martin Root Certification Authority 6,OU=Certification Authority,O=Lockheed Martin Corporation,L=Denver,ST=Colorado,C=US
2025-04-18T22:01:08,961 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Wed Apr 02 16:01:39 UTC 2025
2025-04-18T22:01:08,961 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Fri May 09 16:21:39 UTC 2025
2025-04-18T22:01:08,961 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/134f3cbbdb5d4529a59470b6daac9e4ce22fc10b.crl
2025-04-18T22:01:08,961 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DoD Root CA 6,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-18T22:01:08,961 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Tue Apr 01 17:01:09 UTC 2025
2025-04-18T22:01:08,961 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Fri May 02 17:01:09 UTC 2025
2025-04-18T22:01:08,961 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/7bd6d0fee1b949b75fd1cf734a2ed37d482a0c85.crl
2025-04-18T22:01:08,994 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=Entrust NFI Medium Assurance SSP CA,OU=Certification Authorities,O=Entrust,C=US
2025-04-18T22:01:08,994 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 16:14:23 UTC 2025
2025-04-18T22:01:08,994 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sun Apr 20 16:14:23 UTC 2025
2025-04-18T22:01:08,994 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/61c35bf65d16a11fa2c0920c6a1699fe8fe73302.crl
2025-04-18T22:01:09,097 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=U.S. Department of Transportation Agency CA G6,OU=U.S. Department of Transportation,O=U.S. Government,C=US
2025-04-18T22:01:09,097 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 20:04:24 UTC 2025
2025-04-18T22:01:09,097 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sun Apr 20 20:04:23 UTC 2025
2025-04-18T22:01:09,097 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/fb48aa614980d4f7a68582a244dd97d69ba67105.crl
2025-04-18T22:01:09,097 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Northrop Grumman Corporate Root CA-384,OU=Northrop Grumman Enterprise Services,O=Northrop Grumman Corporation,C=US
2025-04-18T22:01:09,097 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Wed Mar 26 15:40:04 UTC 2025
2025-04-18T22:01:09,097 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sat May 17 16:00:04 UTC 2025
2025-04-18T22:01:09,097 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/9b7fb6290dd1efae32406ff8c2d97cb4c0975096.crl
2025-04-18T22:01:13,659 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=Entrust Managed Services SSP CA,OU=Certification Authorities,O=Entrust,C=US
2025-04-18T22:01:13,659 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 09:00:06 UTC 2025
2025-04-18T22:01:13,659 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sun Apr 20 09:00:06 UTC 2025
2025-04-18T22:01:13,659 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/7561da1f31926e2e2a645ea36519856580e8c72b.crl
2025-04-18T22:01:14,289 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=Department of Veterans Affairs CA,OU=Certification Authorities,OU=Department of Veterans Affairs,O=U.S. Government,C=US
2025-04-18T22:01:14,289 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 21:31:53 UTC 2025
2025-04-18T22:01:14,289 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sat Apr 19 15:31:53 UTC 2025
2025-04-18T22:01:14,289 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/b44ebf67a512f7108473fe378732c6b7b91c483c.crl
2025-04-18T22:01:14,290 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=U.S. Department of Education Agency CA - G5,OU=U.S. Department of Education,O=U.S. Government,C=US
2025-04-18T22:01:14,290 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Tue Apr 08 00:21:07 UTC 2025
2025-04-18T22:01:14,290 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Tue Apr 15 00:21:07 UTC 2025
2025-04-18T22:01:14,290 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/05fb523882b43f00c4190ec12affc44f6e0ebe64.crl
2025-04-18T22:01:14,290 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=WidePoint NFI CA 5,O=ORC PKI,C=US
2025-04-18T22:01:14,290 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 20:10:00 UTC 2025
2025-04-18T22:01:14,290 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sun Apr 20 20:10:01 UTC 2025
2025-04-18T22:01:14,290 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/b1ed8c7dc370542ab24e0070131191b6554020b7.crl
2025-04-18T22:01:14,736 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD EMAIL CA-73,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-18T22:01:14,736 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 08:00:00 UTC 2025
2025-04-18T22:01:14,736 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Fri Apr 25 17:00:00 UTC 2025
2025-04-18T22:01:14,736 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/d7ce284cc8246a56465b75658b67c4fac8e088a5.crl
2025-04-18T22:01:15,109 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=OCIO CA,OU=Certification Authorities,OU=Department of the Treasury,O=U.S. Government,C=US
2025-04-18T22:01:15,110 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Fri Apr 18 21:20:38 UTC 2025
2025-04-18T22:01:15,110 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Sat Apr 19 15:20:38 UTC 2025
2025-04-18T22:01:15,110 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/fadf2301c4aaec23e3ad6f0d34a50dcf3964655e.crl
2025-04-18T22:01:15,110 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=Entrust Managed Services NFI Root CA,OU=Certification Authorities,O=Entrust,C=US
2025-04-18T22:01:15,110 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - thisUpdate: Tue Apr 08 01:02:49 UTC 2025
2025-04-18T22:01:15,110 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - nextUpdate: Thu Apr 10 01:02:49 UTC 2025
```

Most recent build and execution:

```TEXT
tejohnson@Vostro:~/eclipse-workspace/rest-service$ mvn clean package spring-boot:repackage
[INFO] Scanning for projects...
[INFO] 
[INFO] ------------------< org.keysupport.api:rest-service >-------------------
[INFO] Building rest-service 0.0.1-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:3.4.1:clean (default-clean) @ rest-service ---
[INFO] Deleting /home/tejohnson/eclipse-workspace/rest-service/target
[INFO] 
[INFO] --- maven-resources-plugin:3.3.1:resources (default-resources) @ rest-service ---
[INFO] Copying 1 resource from src/main/resources to target/classes
[INFO] Copying 1 resource from src/main/resources to target/classes
[INFO] 
[INFO] --- maven-compiler-plugin:3.13.0:compile (default-compile) @ rest-service ---
[INFO] Recompiling the module because of changed source code.
[INFO] Compiling 52 source files with javac [debug parameters release 17] to target/classes
[INFO] /home/tejohnson/eclipse-workspace/rest-service/src/main/java/org/keysupport/api/pkix/cache/singletons/CRLCacheSingleton.java: /home/tejohnson/eclipse-workspace/rest-service/src/main/java/org/keysupport/api/pkix/cache/singletons/CRLCacheSingleton.java uses unchecked or unsafe operations.
[INFO] /home/tejohnson/eclipse-workspace/rest-service/src/main/java/org/keysupport/api/pkix/cache/singletons/CRLCacheSingleton.java: Recompile with -Xlint:unchecked for details.
[INFO] 
[INFO] --- maven-resources-plugin:3.3.1:testResources (default-testResources) @ rest-service ---
[INFO] skip non existing resourceDirectory /home/tejohnson/eclipse-workspace/rest-service/src/test/resources
[INFO] 
[INFO] --- maven-compiler-plugin:3.13.0:testCompile (default-testCompile) @ rest-service ---
[INFO] Recompiling the module because of changed dependency.
[INFO] Compiling 1 source file with javac [debug parameters release 17] to target/test-classes
[INFO] 
[INFO] --- maven-surefire-plugin:3.5.2:test (default-test) @ rest-service ---
[INFO] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
[INFO] 
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 0, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] 
[INFO] --- maven-jar-plugin:3.4.2:jar (default-jar) @ rest-service ---
[INFO] Building jar: /home/tejohnson/eclipse-workspace/rest-service/target/rest-service-eb.jar
[INFO] 
[INFO] --- spring-boot-maven-plugin:3.4.4:repackage (repackage) @ rest-service ---
[INFO] Replacing main artifact /home/tejohnson/eclipse-workspace/rest-service/target/rest-service-eb.jar with repackaged archive, adding nested dependencies in BOOT-INF/.
[INFO] The original artifact has been renamed to /home/tejohnson/eclipse-workspace/rest-service/target/rest-service-eb.jar.original
[INFO] 
[INFO] --- spring-boot-maven-plugin:3.4.4:repackage (default-cli) @ rest-service ---
[INFO] Replacing main artifact /home/tejohnson/eclipse-workspace/rest-service/target/rest-service-eb.jar with repackaged archive, adding nested dependencies in BOOT-INF/.
[INFO] The original artifact has been renamed to /home/tejohnson/eclipse-workspace/rest-service/target/rest-service-eb.jar.original
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  3.267 s
[INFO] Finished at: 2025-04-19T00:17:40-04:00
[INFO] ------------------------------------------------------------------------
tejohnson@Vostro:~/eclipse-workspace/rest-service$ sudo docker build -t vss .
[+] Building 1.2s (11/11) FINISHED                                                                                                                                                                                           docker:default
 => [internal] load build definition from Dockerfile                                                                                                                                                                                   0.0s
 => => transferring dockerfile: 1.50kB                                                                                                                                                                                                 0.0s
 => [internal] load metadata for registry.access.redhat.com/ubi9/openjdk-21-runtime:latest                                                                                                                                             0.6s
 => [internal] load .dockerignore                                                                                                                                                                                                      0.0s
 => => transferring context: 2B                                                                                                                                                                                                        0.0s
 => [1/6] FROM registry.access.redhat.com/ubi9/openjdk-21-runtime:latest@sha256:b3986db15797dd3ad4cb754e1bf0d75cd346ac63ef5843d810cd75b43d820826                                                                                       0.0s
 => [internal] load build context                                                                                                                                                                                                      0.1s
 => => transferring context: 36.77MB                                                                                                                                                                                                   0.1s
 => CACHED [2/6] RUN echo -e "\n[algorithm_sect]\ndefault_properties = fips=yes" >> /etc/pki/tls/openssl.cnf                                                                                                                           0.0s
 => CACHED [3/6] RUN /bin/rm -rf /opt/jboss                                                                                                                                                                                            0.0s
 => CACHED [4/6] RUN /usr/bin/mkdir /opt/vss                                                                                                                                                                                           0.0s
 => [5/6] ADD target/rest-service-eb.jar /opt/vss/lib/                                                                                                                                                                                 0.1s
 => [6/6] RUN /usr/bin/chown -R 1001 /opt/vss                                                                                                                                                                                          0.2s
 => exporting to image                                                                                                                                                                                                                 0.1s
 => => exporting layers                                                                                                                                                                                                                0.1s
 => => writing image sha256:7cf8e108b633c8965f8d325da5626a26911f8411c3691493ab87b96ecbb3000a                                                                                                                                           0.0s
 => => naming to docker.io/library/vss                                                                                                                                                                                                 0.0s

 2 warnings found (use docker --debug to expand):
 - UndefinedVar: Usage of undefined variable '$PIPELINE_ID' (line 24)
 - UndefinedVar: Usage of undefined variable '$BUILD_DATE' (line 19)
tejohnson@Vostro:~/eclipse-workspace/rest-service$ sudo docker run -it --network host --mount source=crldata,destination=/opt/vss/crls vss:latest
04:17:47.528 [main] INFO org.keysupport.api.RestServiceApplication -- Service Starting

2025-04-19T04:17:47,865 [main] INFO  org.keysupport.api.SystemLog -   .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/


2025-04-19T04:17:47,871 [main] INFO  org.keysupport.api.SystemLog -  :: Spring Boot ::                (v3.4.4)


2025-04-19T04:17:47,959 [main] INFO  o.k.api.RestServiceApplication - Starting RestServiceApplication v0.0.1-SNAPSHOT using Java 21.0.7 with PID 1 (/opt/vss/lib/rest-service-eb.jar started by ? in /home/default)
2025-04-19T04:17:47,960 [main] INFO  o.k.api.RestServiceApplication - No active profile set, falling back to 1 default profile: "default"
2025-04-19T04:17:48,982 [main] INFO  o.s.b.w.e.tomcat.TomcatWebServer - Tomcat initialized with port 5000 (http)
2025-04-19T04:17:48,991 [main] INFO  o.a.coyote.http11.Http11NioProtocol - Initializing ProtocolHandler ["http-nio-5000"]
2025-04-19T04:17:48,992 [main] INFO  o.a.catalina.core.StandardService - Starting service [Tomcat]
2025-04-19T04:17:48,993 [main] INFO  o.a.catalina.core.StandardEngine - Starting Servlet engine: [Apache Tomcat/10.1.39]
2025-04-19T04:17:49,017 [main] INFO  o.a.c.c.C.[Tomcat].[localhost].[/] - Initializing Spring embedded WebApplicationContext
2025-04-19T04:17:49,018 [main] INFO  o.s.b.w.s.c.ServletWebServerApplicationContext - Root WebApplicationContext: initialization completed in 1013 ms
2025-04-19T04:17:49,242 [main] INFO  o.s.v.b.OptionalValidatorFactoryBean - Failed to set up a Bean Validation provider: jakarta.validation.NoProviderFoundException: Unable to create a Configuration, because no Jakarta Bean Validation provider could be found. Add a provider like Hibernate Validator (RI) to your classpath.
2025-04-19T04:17:49,546 [main] INFO  o.a.coyote.http11.Http11NioProtocol - Starting ProtocolHandler ["http-nio-5000"]
2025-04-19T04:17:49,559 [main] INFO  o.s.b.w.e.tomcat.TomcatWebServer - Tomcat started on port 5000 (http) with context path '/'
2025-04-19T04:17:49,591 [main] INFO  o.k.api.RestServiceApplication - Started RestServiceApplication in 2.04 seconds (process running for 2.472)
2025-04-19T04:17:50,126 [scheduling-1] INFO  o.k.a.singletons.HTTPClientSingleton - {"ResponseHeaders":{":status":["200"],"accept-ranges":["bytes"],"access-control-allow-origin":["*"],"cache-control":["max-age=300"],"content-length":["6192"],"content-security-policy":["default-src 'none'; style-src 'unsafe-inline'; sandbox"],"content-type":["text/plain; charset=utf-8"],"cross-origin-resource-policy":["cross-origin"],"date":["Sat, 19 Apr 2025 04:17:50 GMT"],"etag":["\"e2adfe28520290e316263de1cb5547fd891d23f3dd009b02f9a999f459fad638\""],"expires":["Sat, 19 Apr 2025 04:22:50 GMT"],"source-age":["0"],"strict-transport-security":["max-age=31536000"],"vary":["Authorization,Accept-Encoding,Origin"],"via":["1.1 varnish"],"x-cache":["HIT"],"x-cache-hits":["0"],"x-content-type-options":["nosniff"],"x-fastly-request-id":["8b616aa0c0929b26f1aaee6a82e57dfbb87f3bae"],"x-frame-options":["deny"],"x-github-request-id":["6BB1:215C9E:82F266:A877F5:6802614E"],"x-served-by":["cache-ewr-kewr1740078-EWR"],"x-timer":["S1745036270.996032,VS0,VE60"],"x-xss-protection":["1; mode=block"]}}
2025-04-19T04:17:50,126 [scheduling-1] INFO  o.k.a.s.ValidationPoliciesSingleton - {
    "validationPolicies": [
        {
            "validationPolicyId": "c21f969b-5f03-333d-83e0-4f8f136e7682",
            "validationPolicyName": "default",
            "validationPolicyDescription": "Derived from Default SCVP Policy (1.3.6.1.5.5.7.19.1)",
            "trustAnchors": [
                {
                    "x5t#S256": "X5rswkYWshkTcmAN2A9t0yDIyloM638JyYXr8GlpNPw=",
                    "X509SubjectName": "CN=Federal Common Policy CA G2, OU=FPKI, O=U.S. Government, C=US",
                    "x509Certificate": "MIIF3TCCA8WgAwIBAgIUIeW5oMyVbeJ4ygErqP3Fipiz++owDQYJKoZIhvcNAQEMBQAwXDELMAkGA1UEBhMCVVMxGDAWBgNVBAoTD1UuUy4gR292ZXJubWVudDENMAsGA1UECxMERlBLSTEkMCIGA1UEAxMbRmVkZXJhbCBDb21tb24gUG9saWN5IENBIEcyMB4XDTIwMTAxNDEzMzUxMloXDTQwMTAxNDEzMzUxMlowXDELMAkGA1UEBhMCVVMxGDAWBgNVBAoTD1UuUy4gR292ZXJubWVudDENMAsGA1UECxMERlBLSTEkMCIGA1UEAxMbRmVkZXJhbCBDb21tb24gUG9saWN5IENBIEcyMIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA19fTFzEmIRgQKkFty6+99sRRjCTYBYh7LloRpCZs4rgpBk+/5P4aZYd5v01GYBfOKywGJyFh4xk33/Q4yACoOT1uZOloNq/qhhT0r92UogKf77n5JgMhvg/bThVB3lxxahZQMM0YqUhg1rtaKRKsXm0AplhalNT6c3mA3YDSt4+75i105oE3JbsFjDY5DtGMYB9JIhxobtWTSnhL5E5HzO0GVI9UvhWAPVAhxm8oT4wxSOIjZ/MywXflfBrDktZu1PNsJkkYJpvFgDmSFuEPzivcOrytoPiPfgXMqY/P7zO4opLrh2EV5yA4XYEdoyA2dVD8jmm+Lk7zgRFah/84P2guxNtWpZAtQ9Nsag4w4EmtRq82JLqZQlyrMbvLvhWFecEkyfDzwGkFRIOBn1IbUfKTtN5GWpndl8HCUPbR2i7hpV9CFfkXTgsLGTwMNV2xPz2xThrLDu0jrDG+3/k42jB7KH3SQse72yo6MyNF46uumO7vORHlhOTVkWyxotBU327XZfq3BNupUDL6+R4dUG+pQADSstRJ60gePp0IAtQSHZYd1iRiXKpTLl0kofB2Y3LgAFNdYmaHrbrid0dlKIs9QioDwjm+wrDLAmuT4bjLZePhc3qt8ubjhZN2Naz+4YP5+nfSPPClLiyM/UT2el7eY4l6OaqXMIRfJxNIHwcCAwEAAaOBljCBkzAPBgNVHRMBAf8EBTADAQH/MA4GA1UdDwEB/wQEAwIBBjAdBgNVHQ4EFgQU9CdcqcN8R/T6pqewWZeq3TUmF+MwUQYIKwYBBQUHAQsERTBDMEEGCCsGAQUFBzAFhjVodHRwOi8vcmVwby5mcGtpLmdvdi9mY3BjYS9jYUNlcnRzSXNzdWVkQnlmY3BjYWcyLnA3YzANBgkqhkiG9w0BAQwFAAOCAgEAAWQ3MAzwzr3O1RSBkg06NCj7eIL7/I5fwTBLhpoMhE0XoaoPUie0gqRo3KO2MhuBtacjy55ihIY87hShGoKQcbA1fh7e4Cly5QkOY+KbQsltkKzgod2zmPyC0bEOYD2LO141HyeDWdQ6dDXDz6dr8ObntOfMzgdo7vodCMuKU8+ysTdxRxTCi6AVz3uqe5k+ObJYpC0aXHNMy1OnFgL6oxMeGMlSecU/QUAIf0ncDurYFSctFwXitTC0CrcLO9/AGHqTFSHzUrIlbrgd/aGO+E3o3QoU+ThCPPnu1K2KZLG4pyMqdBm4y7rVGPRikLmFhIv/b6b2CL8yiYL0+mJDcrTVs0PYfALtQxMpSA8n053gajlPwhG3O5jcL8SzqlaGPmGqpnEi9aWAYHJXTzbjzGUAc2u8+Kw8Xv4JffhVWIxVKH4NS5PCtgXwxifgrmPi0/uU1w0crclEsSsya7FIBVRTURoSwwda25wIIWPIkQsQK1snJxgEyUzXi10MUDR0WSDqQAdhbOLcmcyhED5hphYQnf8sD8FpoUDjoLCPkU/ytfZoplmcBM4SQ4Ejgjyk63vMqBDcCMXTHciFTsV2e+aReLvIvU4YmaBQQl3vCFj1qMPIkRsTby1Ff8hRDQG3kH0vefcVtcicsdU8kV2Mee/xJ/c0cIHZWMw0HoRZPbo="
                }
            ],
            "userPolicySet": [
                "2.5.29.32.0"
            ],
            "inhibitPolicyMapping": false,
            "requireExplicitPolicy": true,
            "inhibitAnyPolicy": true,
            "cmsIntermediateHintListUri": [
                "https://www.idmanagement.gov/implement/tools/CACertificatesValidatingToFederalCommonPolicyG2.p7b"
            ],
            "excludeIntermediates": []
        },
        {
            "validationPolicyId": "cc54e0ec-49da-333a-8150-2dd00b758b17",
            "validationPolicyName": "aal3",
            "validationPolicyDescription": "Derived from legacy LOA4 validation policy (2.16.840.1.101.10.2.18.2.1.4)",
            "trustAnchors": [
                {
                    "x5t#S256": "X5rswkYWshkTcmAN2A9t0yDIyloM638JyYXr8GlpNPw=",
                    "X509SubjectName": "CN=Federal Common Policy CA G2, OU=FPKI, O=U.S. Government, C=US",
                    "x509Certificate": "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"
                }
            ],
            "userPolicySet": [
                "2.16.840.1.101.3.2.1.3.7",
                "2.16.840.1.101.3.2.1.3.13",
                "2.16.840.1.101.3.2.1.3.16",
                "2.16.840.1.101.3.2.1.3.18",
                "2.16.840.1.101.3.2.1.3.41"
            ],
            "inhibitPolicyMapping": false,
            "requireExplicitPolicy": true,
            "inhibitAnyPolicy": true,
            "cmsIntermediateHintListUri": [
                "https://www.idmanagement.gov/implement/tools/CACertificatesValidatingToFederalCommonPolicyG2.p7b"
            ],
            "excludeIntermediates": []
        }
    ]
}
2025-04-19T04:17:51,142 [scheduling-1] INFO  o.k.a.singletons.HTTPClientSingleton - {"ResponseHeaders":{":status":["200"],"accept-ranges":["bytes"],"access-control-allow-methods":["GET, HEAD"],"cache-control":["max-age=60"],"content-length":["327285"],"content-type":["binary/octet-stream"],"date":["Sat, 19 Apr 2025 04:17:50 GMT"],"etag":["\"a04cc95ad7fb69ed349914918534a955\""],"last-modified":["Thu, 03 Apr 2025 19:28:22 GMT"],"strict-transport-security":["max-age=31536000; preload"],"via":["1.1 2f276f8b7ce92ba7a0844268d20c32ba.cloudfront.net (CloudFront)"],"x-amz-cf-id":["l4shH5L2RGT6NFtAYXV07CyvE-6UZELbiL8BjM_RGovOYY211VwfYw=="],"x-amz-cf-pop":["JFK50-P6"],"x-amz-server-side-encryption":["AES256"],"x-cache":["Miss from cloudfront"],"x-content-type-options":["nosniff"],"x-frame-options":["SAMEORIGIN"],"x-robots-tag":["all"],"x-server":["Cloud.gov Pages"],"x-vcap-request-id":["fb88a3d2-d202-4ba5-503a-6a051f4156a3"],"x-xss-protection":["1; mode=block"]}}
2025-04-19T04:17:51,197 [scheduling-1] INFO  o.k.a.p.c.s.IntermediateCacheSingleton - CMS object contains 144 certificates: https://www.idmanagement.gov/implement/tools/CACertificatesValidatingToFederalCommonPolicyG2.p7b
2025-04-19T04:17:51,358 [scheduling-1] INFO  o.k.a.singletons.HTTPClientSingleton - {"ResponseHeaders":{":status":["200"],"accept-ranges":["bytes"],"access-control-allow-methods":["GET, HEAD"],"age":["1"],"cache-control":["max-age=60"],"content-length":["327285"],"content-type":["binary/octet-stream"],"date":["Sat, 19 Apr 2025 04:17:50 GMT"],"etag":["\"a04cc95ad7fb69ed349914918534a955\""],"last-modified":["Thu, 03 Apr 2025 19:28:22 GMT"],"strict-transport-security":["max-age=31536000; preload"],"via":["1.1 2f276f8b7ce92ba7a0844268d20c32ba.cloudfront.net (CloudFront)"],"x-amz-cf-id":["Pc54JmT8UH-ZYn60Kd3aCmJnFy6zO5uIreSVmSV__nGPM1F5kZz3kA=="],"x-amz-cf-pop":["JFK50-P6"],"x-amz-server-side-encryption":["AES256"],"x-cache":["Hit from cloudfront"],"x-content-type-options":["nosniff"],"x-frame-options":["SAMEORIGIN"],"x-robots-tag":["all"],"x-server":["Cloud.gov Pages"],"x-vcap-request-id":["fb88a3d2-d202-4ba5-503a-6a051f4156a3"],"x-xss-protection":["1; mode=block"]}}
2025-04-19T04:17:51,379 [scheduling-1] INFO  o.k.a.p.c.s.IntermediateCacheSingleton - CMS object contains 144 certificates: https://www.idmanagement.gov/implement/tools/CACertificatesValidatingToFederalCommonPolicyG2.p7b
2025-04-19T04:17:51,385 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/346b0e04f8b2af6525b3efcb5c44392a4c84883f.crl
2025-04-19T04:17:51,393 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Boeing PCA G3,OU=certservers,O=Boeing,C=US
2025-04-19T04:17:51,394 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 698
2025-04-19T04:17:51,394 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 5
2025-04-19T04:17:51,394 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 1
2025-04-19T04:17:51,394 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 698
2025-04-19T04:17:51,394 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 5
2025-04-19T04:17:51,394 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/e58919e4f9a2bce84da12954165181c7d5c28e9c.crl
2025-04-19T04:17:51,642 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=HHS-FPKI-Intermediate-CA-E1,OU=Certification Authorities,OU=HHS,O=U.S. Government,C=US
2025-04-19T04:17:51,642 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 4662042
2025-04-19T04:17:51,798 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 183478
2025-04-19T04:17:51,798 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 2
2025-04-19T04:17:51,798 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 4662740
2025-04-19T04:17:51,798 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 183483
2025-04-19T04:17:51,798 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/335ba56f7a55602b814b2614cc79bf4aba8b32bd.crl
2025-04-19T04:17:51,798 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=ECA Root CA 4,OU=ECA,O=U.S. Government,C=US
2025-04-19T04:17:51,799 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 583
2025-04-19T04:17:51,799 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 6
2025-04-19T04:17:51,799 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 3
2025-04-19T04:17:51,799 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 4663323
2025-04-19T04:17:51,799 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 183489
2025-04-19T04:17:51,799 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/189b7a8bd9fd20851e84a26e31c7e3a990d21382.crl
2025-04-19T04:17:52,162 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD EMAIL CA-72,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-19T04:17:52,163 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 10946922
2025-04-19T04:17:52,325 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 248824
2025-04-19T04:17:52,325 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 4
2025-04-19T04:17:52,325 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 15610245
2025-04-19T04:17:52,325 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 432313
2025-04-19T04:17:52,325 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/0bc04a7e3a6259fe52a077ad6c53c99ff9509ada.crl
2025-04-19T04:17:52,325 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Entrust Managed PKI Federal Root CA G2,OU=Certification Authorities,O=Entrust,C=US
2025-04-19T04:17:52,325 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 754
2025-04-19T04:17:52,325 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 0
2025-04-19T04:17:52,325 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 5
2025-04-19T04:17:52,325 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 15610999
2025-04-19T04:17:52,325 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 432313
2025-04-19T04:17:52,325 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/b5ed2e96044ba918f4f4bd12f1638584975f3e5a.crl
2025-04-19T04:17:52,326 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DigiCert Class 3 SSP Intermediate CA - G4,O=DigiCert\, Inc.,C=US
2025-04-19T04:17:52,326 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 474
2025-04-19T04:17:52,326 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 0
2025-04-19T04:17:52,326 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 6
2025-04-19T04:17:52,326 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 15611473
2025-04-19T04:17:52,326 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 432313
2025-04-19T04:17:52,326 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/2efbc08cab5f57e089f0eafea7643d149027f0c6.crl
2025-04-19T04:17:52,326 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Senate PIV-I CA G5 PROD,OU=Office of the Sergeant at Arms,OU=U.S. Senate,O=U.S. Government,C=US
2025-04-19T04:17:52,326 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 520
2025-04-19T04:17:52,326 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 0
2025-04-19T04:17:52,326 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 7
2025-04-19T04:17:52,326 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 15611993
2025-04-19T04:17:52,326 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 432313
2025-04-19T04:17:52,326 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/8b907d21ebbc1a157bc2b04e7e7351c80eb6dcb6.crl
2025-04-19T04:17:53,316 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD EMAIL CA-64,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-19T04:17:53,317 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 32112660
2025-04-19T04:17:53,739 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 741074
2025-04-19T04:17:53,739 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 8
2025-04-19T04:17:53,739 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 47724653
2025-04-19T04:17:53,739 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 1173387
2025-04-19T04:17:53,739 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/540b11b01df957cfb07a2d5252f3b3a78c52f4ac.crl
2025-04-19T04:17:53,740 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Exostar Federated Identity Service Root CA 2,OU=Certification Authorities,O=Exostar LLC,C=US
2025-04-19T04:17:53,740 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 612
2025-04-19T04:17:53,740 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 1
2025-04-19T04:17:53,740 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 9
2025-04-19T04:17:53,740 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 47725265
2025-04-19T04:17:53,740 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 1173388
2025-04-19T04:17:53,740 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/2fec9e660a6765f3237344b129fc3bae9017acc3.crl
2025-04-19T04:17:53,933 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=HHS-FPKI-Intermediate-CA-E1,OU=Certification Authorities,OU=HHS,O=U.S. Government,C=US
2025-04-19T04:17:53,933 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 4662042
2025-04-19T04:17:54,048 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 183478
2025-04-19T04:17:54,048 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 10
2025-04-19T04:17:54,048 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 52387307
2025-04-19T04:17:54,048 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 1356866
2025-04-19T04:17:54,048 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/4d31ad51d64e577e67693325037ec629a5ddbaf3.crl
2025-04-19T04:17:54,978 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD EMAIL CA-63,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-19T04:17:54,978 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 31317361
2025-04-19T04:17:55,382 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 720537
2025-04-19T04:17:55,382 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 11
2025-04-19T04:17:55,382 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 83704668
2025-04-19T04:17:55,382 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 2077403
2025-04-19T04:17:55,382 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/5610f4841b4a51789c4ae34caf49c12c2d34822a.crl
2025-04-19T04:17:55,393 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=WidePoint ORC NFI 4,OU=Certification Authorities,O=WidePoint,C=US
2025-04-19T04:17:55,393 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 447565
2025-04-19T04:17:55,399 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 10391
2025-04-19T04:17:55,399 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 12
2025-04-19T04:17:55,399 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 84152233
2025-04-19T04:17:55,399 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 2087794
2025-04-19T04:17:55,399 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/56d5901aea95d16fdb2ad10e0efb79750805945e.crl
2025-04-19T04:17:55,401 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=WidePoint ECA 8,OU=Certification Authorities,OU=ECA,O=U.S. Government,C=US
2025-04-19T04:17:55,401 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 70072
2025-04-19T04:17:55,402 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 1462
2025-04-19T04:17:55,402 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 13
2025-04-19T04:17:55,402 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 84222305
2025-04-19T04:17:55,402 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 2089256
2025-04-19T04:17:55,402 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/17e64bc81a4bc9a7a670b44c4d5ec8f636d43098.crl
2025-04-19T04:17:56,199 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD ID CA-63,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-19T04:17:56,199 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 26183641
2025-04-19T04:17:56,543 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 603467
2025-04-19T04:17:56,543 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 14
2025-04-19T04:17:56,543 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 110405946
2025-04-19T04:17:56,543 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 2692723
2025-04-19T04:17:56,543 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/09e478564102a46b20da93e845f631e14cc4c4fc.crl
2025-04-19T04:17:56,543 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Carillon Federal Services PIV-I CA2,OU=Certification Authorities,O=Carillon Federal Services Inc.,C=US
2025-04-19T04:17:56,543 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 993
2025-04-19T04:17:56,543 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 4
2025-04-19T04:17:56,543 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 15
2025-04-19T04:17:56,543 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 110406939
2025-04-19T04:17:56,543 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 2692727
2025-04-19T04:17:56,543 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/62e04838566d6f6b25bebbc38858b1ac9c43c95b.crl
2025-04-19T04:17:57,342 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD ID CA-65,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-19T04:17:57,342 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 25675136
2025-04-19T04:17:57,668 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 601459
2025-04-19T04:17:57,668 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 16
2025-04-19T04:17:57,668 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 136082075
2025-04-19T04:17:57,668 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 3294186
2025-04-19T04:17:57,668 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/85e328cf257aa6018dc56ffec55272aa9ea34c4e.crl
2025-04-19T04:17:57,678 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=U.S. Department of State AD High Assurance CA,CN=AIA,CN=Public Key Services,CN=Services,CN=Configuration,DC=state,DC=sbu
2025-04-19T04:17:57,678 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 726728
2025-04-19T04:17:57,690 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 30519
2025-04-19T04:17:57,690 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 17
2025-04-19T04:17:57,690 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 136808803
2025-04-19T04:17:57,690 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 3324705
2025-04-19T04:17:57,690 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/fb37dd47413f3d7122607f9f8284024009aaca8b.crl
2025-04-19T04:17:57,690 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Verizon SSP CA A2,OU=SSP,O=Verizon,C=US
2025-04-19T04:17:57,690 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 801
2025-04-19T04:17:57,690 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 8
2025-04-19T04:17:57,690 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 18
2025-04-19T04:17:57,690 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 136809604
2025-04-19T04:17:57,690 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 3324713
2025-04-19T04:17:57,691 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/1c21f5e395b1757e06874eb7b0e833b1d88a0b65.crl
2025-04-19T04:17:57,691 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=Entrust Managed Services Root CA,OU=Certification Authorities,O=Entrust,C=US
2025-04-19T04:17:57,691 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 751
2025-04-19T04:17:57,691 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 8
2025-04-19T04:17:57,691 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 19
2025-04-19T04:17:57,691 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 136810355
2025-04-19T04:17:57,691 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 3324721
2025-04-19T04:17:57,691 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/6e7b20044d11c981e9163590d4cbc25b42d60758.crl
2025-04-19T04:17:58,439 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD ID CA-62,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-19T04:17:58,439 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 25687419
2025-04-19T04:17:58,895 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 602309
2025-04-19T04:17:58,895 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 20
2025-04-19T04:17:58,895 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 162497774
2025-04-19T04:17:58,895 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 3927030
2025-04-19T04:17:58,895 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/fe0117a68a2e7a0adb99ee0f4b9483048adc9191.crl
2025-04-19T04:17:58,895 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Carillon PKI Services G2 Root CA 2,OU=Certification Authorities,O=Carillon Information Security Inc.,C=CA
2025-04-19T04:17:58,895 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 824
2025-04-19T04:17:58,895 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 1
2025-04-19T04:17:58,895 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 21
2025-04-19T04:17:58,895 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 162498598
2025-04-19T04:17:58,895 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 3927031
2025-04-19T04:17:58,895 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/23b84eb14e6d24448b4467a765cfa13b399466dc.crl
2025-04-19T04:17:58,896 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=WidePoint ORC SSP 5,O=ORC PKI,C=US
2025-04-19T04:17:58,896 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 37609
2025-04-19T04:17:58,896 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 722
2025-04-19T04:17:58,896 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 22
2025-04-19T04:17:58,896 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 162536207
2025-04-19T04:17:58,896 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 3927753
2025-04-19T04:17:58,896 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/b9b2d8d91cded37bd58e409832764d9c5cd8fda9.crl
2025-04-19T04:17:58,900 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=IdenTrust ECA S23,OU=Certification Authorities,OU=ECA,O=U.S. Government,C=US
2025-04-19T04:17:58,900 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 270470
2025-04-19T04:17:58,903 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 5538
2025-04-19T04:17:58,903 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 23
2025-04-19T04:17:58,903 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 162806677
2025-04-19T04:17:58,903 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 3933291
2025-04-19T04:17:58,903 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/44706febfcab74d7344b73e1b8992b40445bfb1f.crl
2025-04-19T04:17:58,903 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Senate PIV-I CA G6,OU=Office of the Sergeant at Arms,OU=U.S. Senate,O=U.S. Government,C=US
2025-04-19T04:17:58,903 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 7583
2025-04-19T04:17:58,903 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 167
2025-04-19T04:17:58,903 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 24
2025-04-19T04:17:58,903 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 162814260
2025-04-19T04:17:58,903 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 3933458
2025-04-19T04:17:58,903 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/98b6340a1bed049a530a8a0573fa4267cd1066b6.crl
2025-04-19T04:17:59,778 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD EMAIL CA-65,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-19T04:17:59,778 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 32632828
2025-04-19T04:18:00,243 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 752118
2025-04-19T04:18:00,243 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 25
2025-04-19T04:18:00,243 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 195447088
2025-04-19T04:18:00,243 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 4685576
2025-04-19T04:18:00,243 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/c22e258e14f08e58bc2e09afe34812ab60b1861c.crl
2025-04-19T04:18:00,244 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Carillon Federal Services PIV-I CA3,OU=Certification Authorities,O=Carillon Federal Services Inc.,C=US
2025-04-19T04:18:00,244 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 1405
2025-04-19T04:18:00,244 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 16
2025-04-19T04:18:00,244 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 26
2025-04-19T04:18:00,244 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 195448493
2025-04-19T04:18:00,244 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 4685592
2025-04-19T04:18:00,244 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/e376e0a59427e8357df8633cfff3f8565b68172f.crl
2025-04-19T04:18:00,246 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Veterans Affairs CA B3,OU=PKI,OU=Services,DC=va,DC=gov
2025-04-19T04:18:00,246 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 142330
2025-04-19T04:18:00,248 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 3943
2025-04-19T04:18:00,248 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 27
2025-04-19T04:18:00,248 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 195590823
2025-04-19T04:18:00,248 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 4689535
2025-04-19T04:18:00,248 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/a014b1ba644ef3f93716dbe54b91c1845572842e.crl
2025-04-19T04:18:00,263 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=USPTO_INTR_CA1,CN=AIA,CN=Public Key Services,CN=Services,CN=Configuration,DC=uspto,DC=gov
2025-04-19T04:18:00,263 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 777861
2025-04-19T04:18:00,275 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 21005
2025-04-19T04:18:00,276 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 28
2025-04-19T04:18:00,276 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 196368684
2025-04-19T04:18:00,276 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 4710540
2025-04-19T04:18:00,276 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/9d8e6c06639efc183b502dd33bbbda05dd8ca28a.crl
2025-04-19T04:18:00,398 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Leidos FBCA Cloud PKI CA-1,O=Leidos
2025-04-19T04:18:00,398 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 968126
2025-04-19T04:18:00,418 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 27648
2025-04-19T04:18:00,419 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 29
2025-04-19T04:18:00,419 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 197336810
2025-04-19T04:18:00,419 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 4738188
2025-04-19T04:18:00,419 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/7cc34a5cba1f36ab83517df4e0e50e907f1c1341.crl
2025-04-19T04:18:00,951 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=DHS CA4,OU=Certification Authorities,OU=Department of Homeland Security,O=U.S. Government,C=US
2025-04-19T04:18:00,951 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 20092650
2025-04-19T04:18:01,246 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 532014
2025-04-19T04:18:01,246 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 30
2025-04-19T04:18:01,246 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 217429460
2025-04-19T04:18:01,246 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 5270202
2025-04-19T04:18:01,246 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/97696dfd7e2be5a7f262bd75ea961ee00cc0a946.crl
2025-04-19T04:18:01,262 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Raytheon Technologies Medium Assurance CA,OU=Class3-G3,O=CAs,DC=rtx,DC=com
2025-04-19T04:18:01,262 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 911603
2025-04-19T04:18:01,279 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 24659
2025-04-19T04:18:01,279 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 31
2025-04-19T04:18:01,279 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 218341063
2025-04-19T04:18:01,279 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 5294861
2025-04-19T04:18:01,279 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/a2693f3518ed9e21c0130980f4100c04860c56e8.crl
2025-04-19T04:18:01,350 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=U.S. Department of Transportation Agency CA G5,OU=U.S. Department of Transportation,O=U.S. Government,C=US
2025-04-19T04:18:01,350 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 4660048
2025-04-19T04:18:01,399 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 95099
2025-04-19T04:18:01,399 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 32
2025-04-19T04:18:01,399 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 223001111
2025-04-19T04:18:01,399 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 5389960
2025-04-19T04:18:01,399 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/cf94c7db7fdd3390e761412d7010e207ec6ee7aa.crl
2025-04-19T04:18:01,399 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=ECA Root CA 5,OU=ECA,O=U.S. Government,C=US
2025-04-19T04:18:01,399 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 737
2025-04-19T04:18:01,399 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 1
2025-04-19T04:18:01,399 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 33
2025-04-19T04:18:01,399 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 223001848
2025-04-19T04:18:01,399 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 5389961
2025-04-19T04:18:01,399 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/85b5e798a0b71286f7c2bdd2731735c8718b6c0a.crl
2025-04-19T04:18:01,400 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=WidePoint NFI CA 6,O=ORC PKI,C=US
2025-04-19T04:18:01,400 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 91449
2025-04-19T04:18:01,401 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 2320
2025-04-19T04:18:01,401 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 34
2025-04-19T04:18:01,401 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 223093297
2025-04-19T04:18:01,401 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 5392281
2025-04-19T04:18:01,401 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/f8f98b2f7f90439f8fe68c2cb549b84f928b1674.crl
2025-04-19T04:18:01,401 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=IdenTrust Global Common Root CA 1,O=IdenTrust,C=US
2025-04-19T04:18:01,402 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 1260
2025-04-19T04:18:01,402 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 11
2025-04-19T04:18:01,402 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 35
2025-04-19T04:18:01,402 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 223094557
2025-04-19T04:18:01,402 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 5392292
2025-04-19T04:18:01,402 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/59db0f8231ed848c108e47d88dd58eedcafb310a.crl
2025-04-19T04:18:01,513 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Exostar Federated Identity Service Signing CA 4,DC=evincible,DC=com
2025-04-19T04:18:01,514 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 190161
2025-04-19T04:18:01,515 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 3815
2025-04-19T04:18:01,515 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 36
2025-04-19T04:18:01,515 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 223284718
2025-04-19T04:18:01,515 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 5396107
2025-04-19T04:18:01,515 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/8d474ad15e45eaee2f515847214f12ebca7aa15f.crl
2025-04-19T04:18:01,529 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=USPTO_INTR_CA1,CN=AIA,CN=Public Key Services,CN=Services,CN=Configuration,DC=uspto,DC=gov
2025-04-19T04:18:01,529 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 777733
2025-04-19T04:18:01,543 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 21005
2025-04-19T04:18:01,543 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 37
2025-04-19T04:18:01,543 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 224062451
2025-04-19T04:18:01,543 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 5417112
2025-04-19T04:18:01,543 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/66f92598aecbfbe18c008419d485ff9356ead6a6.crl
2025-04-19T04:18:01,571 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=Entrust NFI Medium Assurance SSP CA,OU=Certification Authorities,O=Entrust,C=US
2025-04-19T04:18:01,571 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 1339493
2025-04-19T04:18:01,598 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 44026
2025-04-19T04:18:01,598 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 38
2025-04-19T04:18:01,598 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 225401944
2025-04-19T04:18:01,598 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 5461138
2025-04-19T04:18:01,598 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/7405e1561a81014cd753e70f4ea2e65815304911.crl
2025-04-19T04:18:01,676 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=Social Security Administration Certification Authority,OU=SSA,O=U.S. Government,C=US
2025-04-19T04:18:01,676 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 3610716
2025-04-19T04:18:01,761 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 97531
2025-04-19T04:18:01,761 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 39
2025-04-19T04:18:01,761 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 229012660
2025-04-19T04:18:01,761 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 5558669
2025-04-19T04:18:01,761 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/38801f7767e9973533eeb05e1b072608962745da.crl
2025-04-19T04:18:02,170 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=OCIO CA,OU=Certification Authorities,OU=Department of the Treasury,O=U.S. Government,C=US
2025-04-19T04:18:02,170 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 10891543
2025-04-19T04:18:02,428 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 294345
2025-04-19T04:18:02,428 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 40
2025-04-19T04:18:02,428 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 239904203
2025-04-19T04:18:02,428 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 5853014
2025-04-19T04:18:02,428 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/4954914c69443bc4f8022cf4f82d335689759810.crl
2025-04-19T04:18:02,429 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=Entrust Managed Services Root CA,OU=Certification Authorities,O=Entrust,C=US
2025-04-19T04:18:02,429 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 751
2025-04-19T04:18:02,429 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 8
2025-04-19T04:18:02,429 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 41
2025-04-19T04:18:02,429 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 239904954
2025-04-19T04:18:02,429 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 5853022
2025-04-19T04:18:02,429 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/5e519d99eb82737f86104c5cf8d036c696f42e97.crl
2025-04-19T04:18:03,078 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=DHS CA4,OU=Certification Authorities,OU=Department of Homeland Security,O=U.S. Government,C=US
2025-04-19T04:18:03,079 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 20092650
2025-04-19T04:18:03,475 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 532014
2025-04-19T04:18:03,475 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 42
2025-04-19T04:18:03,475 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 259997604
2025-04-19T04:18:03,475 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 6385036
2025-04-19T04:18:03,475 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/69c8537b0521968a793b118f163d0287b9018772.crl
2025-04-19T04:18:03,616 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=NASA Operational CA,OU=Certification Authorities,OU=NASA,O=U.S. Government,C=US
2025-04-19T04:18:03,616 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 4893289
2025-04-19T04:18:03,720 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 132224
2025-04-19T04:18:03,720 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 43
2025-04-19T04:18:03,720 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 264890893
2025-04-19T04:18:03,720 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 6517260
2025-04-19T04:18:03,720 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/fff8ae138b922b799241a3765c2c819e9ac59c78.crl
2025-04-19T04:18:03,720 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DoD Interoperability Root CA 2,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-19T04:18:03,720 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 615
2025-04-19T04:18:03,720 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 6
2025-04-19T04:18:03,720 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 44
2025-04-19T04:18:03,720 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 264891508
2025-04-19T04:18:03,720 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 6517266
2025-04-19T04:18:03,720 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/e9ba674340ad41229afefa094388f362b86baf2a.crl
2025-04-19T04:18:03,721 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Carillon Federal Services PIV-I CA1,OU=Certification Authorities,O=Carillon Federal Services Inc.,C=US
2025-04-19T04:18:03,722 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 60464
2025-04-19T04:18:03,722 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 1512
2025-04-19T04:18:03,722 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 45
2025-04-19T04:18:03,722 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 264951972
2025-04-19T04:18:03,722 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 6518778
2025-04-19T04:18:03,722 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/9d39ad66b0f2a3b67fe0405992b9517551a2843e.crl
2025-04-19T04:18:03,723 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=WidePoint NFI Root 2,OU=Certification Authorities,O=WidePoint,C=US
2025-04-19T04:18:03,723 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 1220
2025-04-19T04:18:03,723 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 9
2025-04-19T04:18:03,723 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 46
2025-04-19T04:18:03,723 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 264953192
2025-04-19T04:18:03,723 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 6518787
2025-04-19T04:18:03,723 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/83f45f35ebccda5d7b994fc2534418405abdef59.crl
2025-04-19T04:18:03,910 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD ID CA-72,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-19T04:18:03,911 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 6816350
2025-04-19T04:18:04,056 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 159161
2025-04-19T04:18:04,056 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 47
2025-04-19T04:18:04,056 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 271769542
2025-04-19T04:18:04,056 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 6677948
2025-04-19T04:18:04,056 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/344afc53a15b60006684d78b21b90857115a1849.crl
2025-04-19T04:18:04,501 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD EMAIL CA-70,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-19T04:18:04,501 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 11115540
2025-04-19T04:18:04,715 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 259402
2025-04-19T04:18:04,715 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 48
2025-04-19T04:18:04,715 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 282885082
2025-04-19T04:18:04,715 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 6937350
2025-04-19T04:18:04,715 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/af9a21c5b31729aba839953e711575a1f0f51607.crl
2025-04-19T04:18:04,715 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Veterans Affairs User CA B1,OU=PKI,OU=Services,DC=va,DC=gov
2025-04-19T04:18:04,715 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 3149
2025-04-19T04:18:04,715 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 99
2025-04-19T04:18:04,715 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 49
2025-04-19T04:18:04,715 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 282888231
2025-04-19T04:18:04,715 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 6937449
2025-04-19T04:18:04,715 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/02a4bc7fdc3443d8eb3c3b9e90d6f757a9186f50.crl
2025-04-19T04:18:04,843 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=NASA Operational CA,OU=Certification Authorities,OU=NASA,O=U.S. Government,C=US
2025-04-19T04:18:04,843 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 4893033
2025-04-19T04:18:04,966 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 132224
2025-04-19T04:18:04,966 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 50
2025-04-19T04:18:04,966 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 287781264
2025-04-19T04:18:04,966 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 7069673
2025-04-19T04:18:04,966 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/49dac3330bc70f7d48a3b71f0896378984ccfeaa.crl
2025-04-19T04:18:04,973 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=NRC SSP Agency CA G4,OU=U.S. Nuclear Regulatory Commission,O=U.S. Government,C=US
2025-04-19T04:18:04,973 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 351862
2025-04-19T04:18:04,977 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 7171
2025-04-19T04:18:04,977 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 51
2025-04-19T04:18:04,977 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 288133126
2025-04-19T04:18:04,977 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 7076844
2025-04-19T04:18:04,977 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/c080963f8a8080de420f43e6a40fcba76808c8cd.crl
2025-04-19T04:18:05,138 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD ID CA-70,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-19T04:18:05,139 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 6788188
2025-04-19T04:18:05,269 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 158503
2025-04-19T04:18:05,270 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 52
2025-04-19T04:18:05,270 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 294921314
2025-04-19T04:18:05,270 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 7235347
2025-04-19T04:18:05,270 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/a953be6484834b5d26c6273e2ed18468553cd075.crl
2025-04-19T04:18:05,270 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=Entrust Managed Services Root CA,OU=Certification Authorities,O=Entrust,C=US
2025-04-19T04:18:05,270 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 752
2025-04-19T04:18:05,270 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 8
2025-04-19T04:18:05,270 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 53
2025-04-19T04:18:05,271 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 294922066
2025-04-19T04:18:05,271 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 7235355
2025-04-19T04:18:05,271 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/da9cb61fff679d47910d26e72966146597e68058.crl
2025-04-19T04:18:05,901 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=Department of Veterans Affairs CA,OU=Certification Authorities,OU=Department of Veterans Affairs,O=U.S. Government,C=US
2025-04-19T04:18:05,901 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 17717171
2025-04-19T04:18:06,870 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 769492
2025-04-19T04:18:06,870 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 54
2025-04-19T04:18:06,870 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 312639237
2025-04-19T04:18:06,870 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 8004847
2025-04-19T04:18:06,870 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/1d76056a498f1159badf0beb4db2b70cfa26b1c9.crl
2025-04-19T04:18:06,870 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DigiCert Federal SSP Intermediate CA - G6,O=DigiCert\, Inc.,C=US
2025-04-19T04:18:06,870 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 730
2025-04-19T04:18:06,870 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 0
2025-04-19T04:18:06,870 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 55
2025-04-19T04:18:06,870 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 312639967
2025-04-19T04:18:06,870 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 8004847
2025-04-19T04:18:06,870 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/3766be845697cec68414fe3b772de791dc6acdf5.crl
2025-04-19T04:18:06,870 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=NRC PROD G6 Fed SSP CA,OU=U.S. Nuclear Regulatory Commission,O=U.S. Government,C=US
2025-04-19T04:18:06,870 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 1350
2025-04-19T04:18:06,870 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 12
2025-04-19T04:18:06,870 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 56
2025-04-19T04:18:06,870 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 312641317
2025-04-19T04:18:06,870 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 8004859
2025-04-19T04:18:06,870 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/16cdd6ce7fcf17f97e2185f4b1e72c33ff104509.crl
2025-04-19T04:18:06,957 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=Social Security Administration Certification Authority,OU=SSA,O=U.S. Government,C=US
2025-04-19T04:18:06,957 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 3610716
2025-04-19T04:18:07,043 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 97531
2025-04-19T04:18:07,043 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 57
2025-04-19T04:18:07,043 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 316252033
2025-04-19T04:18:07,043 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 8102390
2025-04-19T04:18:07,043 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/cd9a1c6072c1ebbeaec5abac4990eb4d8ef1dfae.crl
2025-04-19T04:18:07,354 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=OCIO CA,OU=Certification Authorities,OU=Department of the Treasury,O=U.S. Government,C=US
2025-04-19T04:18:07,355 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 10891287
2025-04-19T04:18:07,727 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 294345
2025-04-19T04:18:07,728 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 58
2025-04-19T04:18:07,728 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 327143320
2025-04-19T04:18:07,728 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 8396735
2025-04-19T04:18:07,728 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/771441a65d9526d01dff953b628ceab7b55d3b92.crl
2025-04-19T04:18:07,728 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD EMAIL CA-59,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-19T04:18:07,728 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 521
2025-04-19T04:18:07,728 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 2
2025-04-19T04:18:07,728 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 59
2025-04-19T04:18:07,728 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 327143841
2025-04-19T04:18:07,728 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 8396737
2025-04-19T04:18:07,728 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/12e2abe4336b307f3eea6f3e02d61ebc602bf1f5.crl
2025-04-19T04:18:07,756 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=WidePoint ORC NFI 4,OU=Certification Authorities,O=WidePoint,C=US
2025-04-19T04:18:07,756 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 1794153
2025-04-19T04:18:07,778 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 41707
2025-04-19T04:18:07,779 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 60
2025-04-19T04:18:07,779 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 328937994
2025-04-19T04:18:07,779 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 8438444
2025-04-19T04:18:07,779 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/174bb826ba697aad12505745319e57bb74a5da2f.crl
2025-04-19T04:18:07,779 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=US Treasury Root CA,OU=Certification Authorities,OU=Department of the Treasury,O=U.S. Government,C=US
2025-04-19T04:18:07,779 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 1083
2025-04-19T04:18:07,779 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 8
2025-04-19T04:18:07,779 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 61
2025-04-19T04:18:07,779 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 328939077
2025-04-19T04:18:07,779 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 8438452
2025-04-19T04:18:07,779 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/9257e2eb2fb88b486c4ddd07b33c6fa7e53990ce.crl
2025-04-19T04:18:07,779 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Lockheed Martin Root Certification Authority 2,OU=Certification Authorities,O=Lockheed Martin Corporation,L=Denver,ST=Colorado,C=US
2025-04-19T04:18:07,779 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 745
2025-04-19T04:18:07,779 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 3
2025-04-19T04:18:07,779 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 62
2025-04-19T04:18:07,779 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 328939822
2025-04-19T04:18:07,779 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 8438455
2025-04-19T04:18:07,779 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/55b46c333fe3601aa7ffc3edb4f7e404da29d063.crl
2025-04-19T04:18:09,034 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=Entrust Managed Services SSP CA,OU=Certification Authorities,O=Entrust,C=US
2025-04-19T04:18:09,035 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 30144288
2025-04-19T04:18:10,849 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 1287491
2025-04-19T04:18:10,850 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 63
2025-04-19T04:18:10,850 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 359084110
2025-04-19T04:18:10,850 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 9725946
2025-04-19T04:18:10,850 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/d238ddb5ef4b5957367fbfbf9ca67d0c193105ad.crl
2025-04-19T04:18:10,857 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=FTI Certification Authority,OU=FTI PKI Trust Infrastructure,O=Foundation for Trusted Identity,C=US
2025-04-19T04:18:10,857 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 232626
2025-04-19T04:18:10,860 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 6596
2025-04-19T04:18:10,860 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 64
2025-04-19T04:18:10,860 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 359316736
2025-04-19T04:18:10,860 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 9732542
2025-04-19T04:18:10,860 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/6c8a94a277b180721d817a16aaf2dcce66ee45c0.crl
2025-04-19T04:18:10,860 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DoD Root CA 3,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-19T04:18:10,860 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 597
2025-04-19T04:18:10,860 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 6
2025-04-19T04:18:10,860 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 65
2025-04-19T04:18:10,860 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 359317333
2025-04-19T04:18:10,860 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 9732548
2025-04-19T04:18:10,860 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/96bd7a28f20e838a9040eed2a450d77f75a1965c.crl
2025-04-19T04:18:11,420 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Entrust Derived Credential SSP CA,OU=Certification Authorities,O=Entrust,C=US
2025-04-19T04:18:11,421 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 13905812
2025-04-19T04:18:11,989 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 375994
2025-04-19T04:18:11,989 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 66
2025-04-19T04:18:11,989 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 373223145
2025-04-19T04:18:11,989 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 10108542
2025-04-19T04:18:11,989 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/cdc5e6e3e425179770baaa93e2b9be41a3922be7.crl
2025-04-19T04:18:14,029 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD EMAIL CA-62,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-19T04:18:14,029 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 32115647
2025-04-19T04:18:14,638 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 741442
2025-04-19T04:18:14,638 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 67
2025-04-19T04:18:14,638 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 405338792
2025-04-19T04:18:14,638 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 10849984
2025-04-19T04:18:14,638 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/e8a404029e49d215d1a55fbbeb943904bb82296e.crl
2025-04-19T04:18:14,639 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=IdenTrust ECA S22,OU=Certification Authorities,OU=ECA,O=U.S. Government,C=US
2025-04-19T04:18:14,639 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 2759
2025-04-19T04:18:14,639 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 46
2025-04-19T04:18:14,639 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 68
2025-04-19T04:18:14,639 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 405341551
2025-04-19T04:18:14,639 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 10850030
2025-04-19T04:18:14,639 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/2a3751d8b481044fa64014981b8495765ea6bb60.crl
2025-04-19T04:18:14,644 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Lockheed Martin Certification Authority 6 G3,OU=Certification Authorities,O=Lockheed Martin Corporation,C=US
2025-04-19T04:18:14,644 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 265733
2025-04-19T04:18:14,648 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 6935
2025-04-19T04:18:14,648 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 69
2025-04-19T04:18:14,648 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 405607284
2025-04-19T04:18:14,648 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 10856965
2025-04-19T04:18:14,648 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/9025399a59fc0c7557b9cd6e4c126175d93ae961.crl
2025-04-19T04:18:14,815 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD ID CA-71,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-19T04:18:14,815 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 6817438
2025-04-19T04:18:14,951 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 159177
2025-04-19T04:18:14,952 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 70
2025-04-19T04:18:14,952 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 412424722
2025-04-19T04:18:14,952 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 11016142
2025-04-19T04:18:14,952 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/7509a61513ae873cfa739400f2f0f579b9b27214.crl
2025-04-19T04:18:14,952 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD ID CA-59,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-19T04:18:14,952 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 1767
2025-04-19T04:18:14,952 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 57
2025-04-19T04:18:14,952 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 71
2025-04-19T04:18:14,952 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 412426489
2025-04-19T04:18:14,952 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 11016199
2025-04-19T04:18:14,952 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/f3ed39b9da1b4d033c261539d833b508ef383e39.crl
2025-04-19T04:18:14,952 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=Entrust Managed Services NFI Root CA,OU=Certification Authorities,O=Entrust,C=US
2025-04-19T04:18:14,952 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 569
2025-04-19T04:18:14,952 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 3
2025-04-19T04:18:14,952 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 72
2025-04-19T04:18:14,952 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 412427058
2025-04-19T04:18:14,952 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 11016202
2025-04-19T04:18:14,953 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/08893ace13bc1cf23a2d98310ba9fe3879fd8222.crl
2025-04-19T04:18:15,750 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD DERILITY CA-1,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-19T04:18:15,750 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 18912877
2025-04-19T04:18:16,364 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 859654
2025-04-19T04:18:16,364 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 73
2025-04-19T04:18:16,364 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 431339935
2025-04-19T04:18:16,364 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 11875856
2025-04-19T04:18:16,364 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/ed186c1cf230862f832d515e2e8f446beb8e6c60.crl
2025-04-19T04:18:16,845 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD EMAIL CA-71,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-19T04:18:16,845 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 11134623
2025-04-19T04:18:20,447 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 259222
2025-04-19T04:18:20,447 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 74
2025-04-19T04:18:20,447 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 442474558
2025-04-19T04:18:20,447 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 12135078
2025-04-19T04:18:20,447 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/79f00049eb7f77c25d410265348a90239b1e076f.crl
2025-04-19T04:18:20,448 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Federal Bridge CA G4,OU=FPKI,O=U.S. Government,C=US
2025-04-19T04:18:20,448 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 950
2025-04-19T04:18:20,448 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 9
2025-04-19T04:18:20,448 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 75
2025-04-19T04:18:20,448 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 442475508
2025-04-19T04:18:20,448 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 12135087
2025-04-19T04:18:20,448 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/0b7ade179aade67ad55c6c3a7b16f762e4a1901d.crl
2025-04-19T04:18:20,553 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD DERILITY CA-3,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-19T04:18:20,553 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 3890521
2025-04-19T04:18:20,670 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 177237
2025-04-19T04:18:20,670 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 76
2025-04-19T04:18:20,670 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 446366029
2025-04-19T04:18:20,670 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 12312324
2025-04-19T04:18:20,670 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/4b1801bb5afa8aebdef4161e44ea440502a05af3.crl
2025-04-19T04:18:21,175 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Entrust Derived Credential SSP CA,OU=Certification Authorities,O=Entrust,C=US
2025-04-19T04:18:21,175 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 13905812
2025-04-19T04:18:21,807 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 375994
2025-04-19T04:18:21,807 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 77
2025-04-19T04:18:21,807 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 460271841
2025-04-19T04:18:21,807 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 12688318
2025-04-19T04:18:21,807 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/7a8b3c0692dc1ea8d282ac1b746f743d4ed1a89b.crl
2025-04-19T04:18:21,808 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=CertiPath Bridge CA - G3,OU=Certification Authorities,O=CertiPath,C=US
2025-04-19T04:18:21,808 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 742
2025-04-19T04:18:21,808 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 0
2025-04-19T04:18:21,808 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 78
2025-04-19T04:18:21,808 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 460272583
2025-04-19T04:18:21,808 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 12688318
2025-04-19T04:18:21,808 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/cd1ec7e1fb18e5c9a8aefa6673655649ff06bb85.crl
2025-04-19T04:18:21,808 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD DERILITY CA-4,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-19T04:18:21,808 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 500
2025-04-19T04:18:21,808 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 1
2025-04-19T04:18:21,808 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 79
2025-04-19T04:18:21,808 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 460273083
2025-04-19T04:18:21,808 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 12688319
2025-04-19T04:18:21,808 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/cc006861a6a50393100a1b61b78718c14556da82.crl
2025-04-19T04:18:21,808 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=U.S. Department of State AD Root CA,CN=AIA,CN=Public Key Services,CN=Services,CN=Configuration,DC=state,DC=sbu
2025-04-19T04:18:21,808 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 818
2025-04-19T04:18:21,808 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 0
2025-04-19T04:18:21,808 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 80
2025-04-19T04:18:21,808 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 460273901
2025-04-19T04:18:21,808 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 12688319
2025-04-19T04:18:21,808 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/8cd6d469a9e485413a6aa65eda511a178d928b6c.crl
2025-04-19T04:18:22,454 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=U.S. Department of State PIV CA2,OU=Certification Authorities,OU=PIV,OU=Department of State,O=U.S. Government,C=US
2025-04-19T04:18:22,454 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 18359423
2025-04-19T04:18:22,799 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 493164
2025-04-19T04:18:22,799 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 81
2025-04-19T04:18:22,799 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 478633324
2025-04-19T04:18:22,799 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 13181483
2025-04-19T04:18:22,799 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/d7ce9df984d682574316bbcad094f9cef6a77b40.crl
2025-04-19T04:18:22,906 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=Social Security Administration Certification Authority,OU=SSA,O=U.S. Government,C=US
2025-04-19T04:18:22,906 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 3610972
2025-04-19T04:18:23,015 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 97531
2025-04-19T04:18:23,015 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 82
2025-04-19T04:18:23,015 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 482244296
2025-04-19T04:18:23,015 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 13279014
2025-04-19T04:18:23,015 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/8b2307b78e0a20f236f6b96134bfb8f5c36bbee9.crl
2025-04-19T04:18:23,016 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=WidePoint SSP Intermediate CA,O=ORC PKI,C=US
2025-04-19T04:18:23,016 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 914
2025-04-19T04:18:23,016 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 2
2025-04-19T04:18:23,016 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 83
2025-04-19T04:18:23,016 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 482245210
2025-04-19T04:18:23,016 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 13279016
2025-04-19T04:18:23,016 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/182e2021b3c957858827e78a7584f373c677e309.crl
2025-04-19T04:18:23,757 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=DHS CA4,OU=Certification Authorities,OU=Department of Homeland Security,O=U.S. Government,C=US
2025-04-19T04:18:23,758 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 20092906
2025-04-19T04:18:24,145 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 532014
2025-04-19T04:18:24,145 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 84
2025-04-19T04:18:24,145 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 502338116
2025-04-19T04:18:24,145 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 13811030
2025-04-19T04:18:24,145 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/f4275ca9c37c47f4faa6a7b05997aadd352617e3.crl
2025-04-19T04:18:24,145 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Federal Common Policy CA G2,OU=FPKI,O=U.S. Government,C=US
2025-04-19T04:18:24,145 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 1212
2025-04-19T04:18:24,145 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 9
2025-04-19T04:18:24,145 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 85
2025-04-19T04:18:24,145 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 502339328
2025-04-19T04:18:24,145 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 13811039
2025-04-19T04:18:24,145 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/e6dd1a071acb6bba20b9963993f814dc98033727.crl
2025-04-19T04:18:25,496 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=Entrust Managed Services SSP CA,OU=Certification Authorities,O=Entrust,C=US
2025-04-19T04:18:25,496 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 30144288
2025-04-19T04:18:27,056 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 1287491
2025-04-19T04:18:27,056 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 86
2025-04-19T04:18:27,056 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 532483616
2025-04-19T04:18:27,056 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 15098530
2025-04-19T04:18:27,056 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/19c1ce87493380b6f75aac65c374f07f3792a561.crl
2025-04-19T04:18:27,056 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=STRAC Bridge Root Certification Authority,OU=STRAC PKI Trust Infrastructure,O=STRAC,C=US
2025-04-19T04:18:27,056 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 1107
2025-04-19T04:18:27,056 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 10
2025-04-19T04:18:27,056 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 87
2025-04-19T04:18:27,056 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 532484723
2025-04-19T04:18:27,056 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 15098540
2025-04-19T04:18:27,056 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/688415488c54707f2d12580eec1c78ef3c2e5964.crl
2025-04-19T04:18:27,057 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=US Treasury Root CA,OU=Certification Authorities,OU=Department of the Treasury,O=U.S. Government,C=US
2025-04-19T04:18:27,057 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 827
2025-04-19T04:18:27,057 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 8
2025-04-19T04:18:27,057 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 88
2025-04-19T04:18:27,057 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 532485550
2025-04-19T04:18:27,057 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 15098548
2025-04-19T04:18:27,057 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/08e55d81ae79141cbc18a0c10602ff1eaa94bced.crl
2025-04-19T04:18:27,070 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=IGC CA 1,OU=IdenTrust Global Common,O=IdenTrust,C=US
2025-04-19T04:18:27,071 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 775161
2025-04-19T04:18:27,085 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 16565
2025-04-19T04:18:27,085 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 89
2025-04-19T04:18:27,085 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 533260711
2025-04-19T04:18:27,085 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 15115113
2025-04-19T04:18:27,085 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/422ae64ac12c3b0e71689297e0130f760acb947d.crl
2025-04-19T04:18:27,085 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOE SSP CA,OU=Certification Authorities,OU=Department of Energy,O=U.S. Government,C=US
2025-04-19T04:18:27,085 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 787
2025-04-19T04:18:27,085 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 10
2025-04-19T04:18:27,085 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 90
2025-04-19T04:18:27,085 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 533261498
2025-04-19T04:18:27,085 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 15115123
2025-04-19T04:18:27,085 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/5719e5d8d6acde78e242f5e445b4d939930bbdda.crl
2025-04-19T04:18:27,085 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DigiCert Federal SSP Intermediate CA - G5,O=DigiCert\, Inc.,C=US
2025-04-19T04:18:27,085 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 474
2025-04-19T04:18:27,085 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 0
2025-04-19T04:18:27,085 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 91
2025-04-19T04:18:27,085 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 533261972
2025-04-19T04:18:27,085 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 15115123
2025-04-19T04:18:27,085 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/e9217bf2146f3855887aa050099e91721c4bd93b.crl
2025-04-19T04:18:27,376 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD ID CA-73,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-19T04:18:27,377 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 5495302
2025-04-19T04:18:27,456 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 129905
2025-04-19T04:18:27,456 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 92
2025-04-19T04:18:27,456 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 538757274
2025-04-19T04:18:27,456 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 15245028
2025-04-19T04:18:27,456 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/00e9dc6dd24acb90bc490d71098bd983ba61dcf2.crl
2025-04-19T04:18:27,972 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=Department of Veterans Affairs CA,OU=Certification Authorities,OU=Department of Veterans Affairs,O=U.S. Government,C=US
2025-04-19T04:18:27,972 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 17717427
2025-04-19T04:18:28,919 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 769492
2025-04-19T04:18:28,919 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 93
2025-04-19T04:18:28,919 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 556474701
2025-04-19T04:18:28,919 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 16014520
2025-04-19T04:18:28,919 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/976248c067692c4c146089d14a003ca39fffa0ae.crl
2025-04-19T04:18:28,919 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=IdenTrust ECA S22C,OU=Certification Authorities,OU=ECA,O=U.S. Government,C=US
2025-04-19T04:18:28,919 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 501
2025-04-19T04:18:28,919 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 0
2025-04-19T04:18:28,919 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 94
2025-04-19T04:18:28,919 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 556475202
2025-04-19T04:18:28,919 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 16014520
2025-04-19T04:18:28,919 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/57bcc05edb89ab3bdd47a9721eaecb42196b1e7a.crl
2025-04-19T04:18:29,678 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD ID CA-64,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-19T04:18:29,678 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 25871940
2025-04-19T04:18:30,144 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 604831
2025-04-19T04:18:30,144 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 95
2025-04-19T04:18:30,144 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 582347142
2025-04-19T04:18:30,144 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 16619351
2025-04-19T04:18:30,144 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/5dbd71271d66c286dd05ba17490b4d83903e989e.crl
2025-04-19T04:18:30,144 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Lockheed Martin Root Certification Authority 6,OU=Certification Authority,O=Lockheed Martin Corporation,L=Denver,ST=Colorado,C=US
2025-04-19T04:18:30,144 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 866
2025-04-19T04:18:30,144 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 0
2025-04-19T04:18:30,144 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 96
2025-04-19T04:18:30,144 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 582348008
2025-04-19T04:18:30,144 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 16619351
2025-04-19T04:18:30,145 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/134f3cbbdb5d4529a59470b6daac9e4ce22fc10b.crl
2025-04-19T04:18:30,145 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DoD Root CA 6,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-19T04:18:30,145 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 751
2025-04-19T04:18:30,145 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 1
2025-04-19T04:18:30,145 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 97
2025-04-19T04:18:30,145 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 582348759
2025-04-19T04:18:30,145 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 16619352
2025-04-19T04:18:30,145 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/7bd6d0fee1b949b75fd1cf734a2ed37d482a0c85.crl
2025-04-19T04:18:30,180 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=Entrust NFI Medium Assurance SSP CA,OU=Certification Authorities,O=Entrust,C=US
2025-04-19T04:18:30,180 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 1339493
2025-04-19T04:18:30,220 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 44026
2025-04-19T04:18:30,220 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 98
2025-04-19T04:18:30,220 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 583688252
2025-04-19T04:18:30,220 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 16663378
2025-04-19T04:18:30,220 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/61c35bf65d16a11fa2c0920c6a1699fe8fe73302.crl
2025-04-19T04:18:30,328 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=U.S. Department of Transportation Agency CA G6,OU=U.S. Department of Transportation,O=U.S. Government,C=US
2025-04-19T04:18:30,328 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 3995199
2025-04-19T04:18:30,502 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 101677
2025-04-19T04:18:30,502 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 99
2025-04-19T04:18:30,502 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 587683451
2025-04-19T04:18:30,502 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 16765055
2025-04-19T04:18:30,502 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/fb48aa614980d4f7a68582a244dd97d69ba67105.crl
2025-04-19T04:18:30,503 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=Northrop Grumman Corporate Root CA-384,OU=Northrop Grumman Enterprise Services,O=Northrop Grumman Corporation,C=US
2025-04-19T04:18:30,503 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 995
2025-04-19T04:18:30,503 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 3
2025-04-19T04:18:30,503 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 100
2025-04-19T04:18:30,503 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 587684446
2025-04-19T04:18:30,503 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 16765058
2025-04-19T04:18:30,503 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/9b7fb6290dd1efae32406ff8c2d97cb4c0975096.crl
2025-04-19T04:18:31,993 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=Entrust Managed Services SSP CA,OU=Certification Authorities,O=Entrust,C=US
2025-04-19T04:18:31,993 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 30144288
2025-04-19T04:18:37,331 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 1287491
2025-04-19T04:18:37,331 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 101
2025-04-19T04:18:37,331 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 617828734
2025-04-19T04:18:37,331 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 18052549
2025-04-19T04:18:37,331 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/7561da1f31926e2e2a645ea36519856580e8c72b.crl
2025-04-19T04:18:38,028 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=Department of Veterans Affairs CA,OU=Certification Authorities,OU=Department of Veterans Affairs,O=U.S. Government,C=US
2025-04-19T04:18:38,028 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 17717171
2025-04-19T04:18:39,172 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 769492
2025-04-19T04:18:39,172 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 102
2025-04-19T04:18:39,172 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 635545905
2025-04-19T04:18:39,172 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 18822041
2025-04-19T04:18:39,172 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/b44ebf67a512f7108473fe378732c6b7b91c483c.crl
2025-04-19T04:18:39,173 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=U.S. Department of Education Agency CA - G5,OU=U.S. Department of Education,O=U.S. Government,C=US
2025-04-19T04:18:39,173 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 3461
2025-04-19T04:18:39,173 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 60
2025-04-19T04:18:39,173 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 103
2025-04-19T04:18:39,173 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 635549366
2025-04-19T04:18:39,173 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 18822101
2025-04-19T04:18:39,173 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/05fb523882b43f00c4190ec12affc44f6e0ebe64.crl
2025-04-19T04:18:39,173 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=WidePoint NFI CA 5,O=ORC PKI,C=US
2025-04-19T04:18:39,173 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 24978
2025-04-19T04:18:39,174 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 325
2025-04-19T04:18:39,174 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 104
2025-04-19T04:18:39,174 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 635574344
2025-04-19T04:18:39,174 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 18822426
2025-04-19T04:18:39,174 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/b1ed8c7dc370542ab24e0070131191b6554020b7.crl
2025-04-19T04:18:39,401 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: CN=DOD EMAIL CA-73,OU=PKI,OU=DoD,O=U.S. Government,C=US
2025-04-19T04:18:39,401 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 8989746
2025-04-19T04:18:39,581 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 211095
2025-04-19T04:18:39,581 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 105
2025-04-19T04:18:39,581 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 644564090
2025-04-19T04:18:39,581 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 19033521
2025-04-19T04:18:39,581 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/d7ce284cc8246a56465b75658b67c4fac8e088a5.crl
2025-04-19T04:18:40,125 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=OCIO CA,OU=Certification Authorities,OU=Department of the Treasury,O=U.S. Government,C=US
2025-04-19T04:18:40,125 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 10891287
2025-04-19T04:18:40,477 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 294345
2025-04-19T04:18:40,478 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 106
2025-04-19T04:18:40,478 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 655455377
2025-04-19T04:18:40,478 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 19327866
2025-04-19T04:18:40,478 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - Loadinng CRL: /opt/vss/crls/fadf2301c4aaec23e3ad6f0d34a50dcf3964655e.crl
2025-04-19T04:18:40,478 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - CRL Issuer: OU=Entrust Managed Services NFI Root CA,OU=Certification Authorities,O=Entrust,C=US
2025-04-19T04:18:40,478 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - crlSize: 569
2025-04-19T04:18:40,478 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - revokedCertCount: 3
2025-04-19T04:18:40,478 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrls: 107
2025-04-19T04:18:40,478 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlSize: 655455946
2025-04-19T04:18:40,478 [scheduling-1] INFO  o.k.a.p.c.s.CRLCacheSingleton - totalCrlRevoked: 19327869

```
