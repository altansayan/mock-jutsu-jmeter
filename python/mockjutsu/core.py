import random
import uuid
import time
import hmac
import hashlib

class MockJutsu:
    """The core engine for mock-jutsu data generation."""
    
    def __init__(self, locale='TR'):
        self.locale = locale.upper()
        self._current_profile = None

    def get_uuid(self):
        """Returns a general UUID v4."""
        return str(uuid.uuid4())

    def get_timestamp(self):
        """Returns current Unix epoch timestamp."""
        return str(int(time.time()))

    def get_signature(self, secret, payload):
        """Generates an HMAC-SHA256 signature for the given payload and secret."""
        signature = hmac.new(
            secret.encode('utf-8'),
            payload.encode('utf-8'),
            hashlib.sha256
        ).hexdigest()
        return signature

    def get_browser_data(self):
        """
        Returns coherent browser data (Name, Version, Engine, UA).
        This ensures that if you ask for BrowserName and UA in the same session, they match.
        """
        browsers = [
            {
                "name": "Chrome",
                "engine": "Blink",
                "versions": ["124.0.6367.201", "123.0.6312.122", "122.0.6261.128"],
                "ua_template": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/{version} Safari/537.36"
            },
            {
                "name": "Firefox",
                "engine": "Gecko",
                "versions": ["126.0", "125.0.3", "124.0.2"],
                "ua_template": "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:{version}) Gecko/20100101 Firefox/{version}"
            },
            {
                "name": "Safari",
                "engine": "WebKit",
                "versions": ["17.5", "17.4.1", "17.0"],
                "ua_template": "Mozilla/5.0 (Macintosh; Intel Mac OS X 14_5) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/{version} Safari/605.1.15"
            },
            {
                "name": "Edge",
                "engine": "Blink",
                "versions": ["124.0.2478.80", "123.0.2420.97"],
                "ua_template": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/{version} Safari/537.36 Edg/{version}"
            }
        ]
        
        # In a real session-based call, we would store this in self._current_profile
        selected = random.choice(browsers)
        version = random.choice(selected["versions"])
        
        return {
            "name": selected["name"],
            "engine": selected["engine"],
            "version": version,
            "ua": selected["ua_template"].format(version=version)
        }

    def generate(self, data_type, *args):
        """Master generation function."""
        dt = data_type.lower()
        
        if dt == 'uuid':
            return self.get_uuid()
        elif dt == 'timestamp':
            return self.get_timestamp()
        elif dt == 'signature':
            return self.get_signature(args[0], args[1]) if len(args) >= 2 else "MISSING_PARAMS"
        elif dt == 'browsername':
            return self.get_browser_data()["name"]
        elif dt == 'ua':
            return self.get_browser_data()["ua"]
        
        return f"UNKNOWN_TYPE: {data_type}"

# Example Usage
if __name__ == "__main__":
    jutsu = MockJutsu()
    print(f"UUID: {jutsu.generate('UUID')}")
    print(f"Browser: {jutsu.generate('BrowserName')}")
    print(f"UA: {jutsu.generate('UA')}")
