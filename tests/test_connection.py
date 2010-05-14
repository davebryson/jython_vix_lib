
import unittest
import vix_api
from com.vmware.vix import VixHandle

class TestConnection(unittest.TestCase):
    
    def setUp(self):
        self.vm_host_handle = VixHandle.VIX_INVALID_HANDLE
        
    def tearDown(self):
        vix_api.disconnect_host(self.vm_host_handle)
        
    def test_connection(self):
        self.vm_host_handle = vix_api.connect_host("127.0.0.1","root","passw0rd")
        self.assertNotEqual(self.vm_host_handle,VixHandle.VIX_INVALID_HANDLE)

if __name__ == '__main__':
    unittest.main()
