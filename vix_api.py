
from com.vmware.vix import *
import sys

"""
Vix api for Jython.  Uses the vixjava library under the covers
"""

def croak(msg):
    """
    Helper to log errors and die
    msg: message to log
    """
    m = "VIX: " % msg
    print(m)
    sys.exit(m)

def connect_host(host,un,pw):
    """
    Connect to ESX Server
    host: esx server hostname/ip
    un  : username
    pw  : password
    
    On success returns a valid host_handle OR dies
    """
    try:
        return VixVSphereHandle(host,un,pw)
    except VixException:
        croak("Error connecting to host")

def disconnect_host(host_handle):
    """ 
    Disconnect from the ESX Server
    
    host_handle: the handle to disconnect
    returns VIX_INVALID_HANDLE
    """
    if host_handle.equals(VixHandle.VIX_INVALID_HANDLE):
        return host_handle
    else:
        host_handle.disconnect()
        return VixHandle.VIX_INVALID_HANDLE
        
def connect_vm(host_handle,config_file):
    """
    Connect to a VM on host

    host_handle: the connection to the ESX host
    config_file: the full path to the .vmx file of the VM to connect with
    return VM_HANDLE on success or dies
    """
    if not host_handle or host_handle.equals(VixHandle.VIX_INVALID_HANDLE):
        croak("Invalid Host Handle")
    try:
        return vm_handle = host_handle.openVm(config_file)
    except VixException:
        croak("Error connection to VM %s" % config_file)

def disconnect_vm(vm_handle):
    """
    Disconnect from the VM
    
    vm_handle: connection to VM
    returns VIX_INVALID_HANDLE
    """
    if not vm_handle or vm_handle.equals(VixHandle.VIX_INVALID_HANDLE):
        pass
    else:
        vm_handle.release()
    
    return VixHandle.VIX_INVALID_HANDLE
