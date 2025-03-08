package com.vpnforms.utils

object Constants {
    // Intent extras
    const val EXTRA_URL = "extra_url"
    
    // VPN configuration - Encrypted
    private val VPN_ADDRESS_ENC = "2Bx5ESr0GokfjiRrdZyByQ=="
    private val VPN_DNS_ENC = "VmE/AltowhMlZUPzQo7v4Q=="
    
    val VPN_ADDRESS: String
        get() = StringEncryption.decrypt(VPN_ADDRESS_ENC)
    
    val VPN_DNS: String
        get() = StringEncryption.decrypt(VPN_DNS_ENC)

    // Encrypted GitHub URL
    private const val ENCRYPTED_GITHUB_RAW_URL = "nZOkuz1G2k9YpbsjNmANZ8DK4vbfc+x1jUFYmYjBSDf4AOmWxFa1iA1vwNq77VA2h1yFR007BWQqIj78A87/JPWI0m5CQy/ZFcDvKNMk/YM="
    
    val GITHUB_RAW_URL: String
        get() = try {
            CryptoUtil.decrypt(ENCRYPTED_GITHUB_RAW_URL)
        } catch (e: Exception) {
            "https://raw.githubusercontent.com/default/repo/main/forms.json"
        }

    // Google IP ranges - Encrypted
    private val ENCRYPTED_IP_RANGES = listOf(
        "MKYB8ToNV2y2pihUka+4iA==",
        "LejieJHXEY4sngn09qdIvA==",
        "9EOa7RXei6GIe9hf67l+uQ==",
        "r+Asl1zpfPBtXdXN8jAe7A==",
        "SokvwUjCE4HjTA0JzGmImA==",
        "gQ5//f+dMER8ey0vpnKQVQ==",
        "PcJdH4OvC8iKZ6tAH5Mtig==",
        "eliVA56r5253RYck02pr2w==",
        "3VfQMDUZ9aTmkJhkDGlsag==",
        "+YUv4FfX9cpQxvmr5Mvqhg=="
    )

    // Essential Google Form domains - Encrypted
    private val ENCRYPTED_ALLOWED_DOMAINS = listOf(
        "m5VgekAp38MQXcqUjmEraw==",
        "NQnhQVYRa6SW+z6jhUcBXw==",
        "Lqwg+mf6Yw14sxuSiZvfuu1LLetcLjggkAgD5RGrKoI=",
        "sc6pOgMapLu6EgKcLskS1z2Gr4WC8Opcwm6y1EbFQec=",
        "6/c7hZSwfYOCwso7wBdlCg==",
        "OU+C1zhXoVUSxKCI/bWkJQ==",
        "MLxcPgJ+mC/AceODkC2RbmfJjWIKS0nB9Ryszhf8vNU=",
        "EalZZFwaU1HlAVWanxb5/4TFHwCcIyRj8LBkz9I3+HA="
    )

    // Block all other Google services - Encrypted
    private val ENCRYPTED_BLOCKED_DOMAINS = listOf(
        "2GnHnERlJh8ogNwyD4C2OQ==",
        "WW8DyIK4r7G6RwRkYdskaoOMBREP6Zp6y8oV8KssTxQ=",
        "TrjK7teVYdueTsEN0N5YXEewaGxFNySVm3n5IyyeeNY=",
        "ZAz5zrEtEGGGpGtwqvLqmg==",
        "9xmz3Y0KSXjxr08F/zeV7g==",
        "5QNqlVibzICaL+LXSd2FCH0YJ34Xwa53CTtbuLkRPVg=",
        "/2mkB99YB8CUgT/K48OmJg==",
        "04UfkNHb40U+72Q8hd/c9A==",
        "QU46YB2BVu1/kxeNSKcQ3g==",
        "gRp6Wc3iAP4/60ymmqrKsQ=="
    )

    // Decrypted getters
    val GOOGLE_IP_RANGES: List<String>
        get() = ENCRYPTED_IP_RANGES.map { StringEncryption.decrypt(it) }

    val ALLOWED_DOMAINS: List<String>
        get() = ENCRYPTED_ALLOWED_DOMAINS.map { StringEncryption.decrypt(it) }

    val BLOCKED_DOMAINS: List<String>
        get() = ENCRYPTED_BLOCKED_DOMAINS.map { StringEncryption.decrypt(it) }

    // Required domains for form functionality
    val REQUIRED_DOMAINS: List<String>
        get() = listOf(
            StringEncryption.decrypt("6/c7hZSwfYOCwso7wBdlCg=="),  // ssl.gstatic.com
            StringEncryption.decrypt("OU+C1zhXoVUSxKCI/bWkJQ=="),  // www.gstatic.com
            StringEncryption.decrypt("MLxcPgJ+mC/AceODkC2RbmfJjWIKS0nB9Ryszhf8vNU="),  // fonts.gstatic.com
            StringEncryption.decrypt("EalZZFwaU1HlAVWanxb5/4TFHwCcIyRj8LBkz9I3+HA=")   // fonts.googleapis.com
        )
}
