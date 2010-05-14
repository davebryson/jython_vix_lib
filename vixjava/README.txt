Welcome to the VIX Java Toolkit

Official project website: http://vixjava.sourceforge.net/

WHAT IS VIX JAVA?

VIX Java uses the VMware VIX API to automate various virtual machine (VM) 
operations. Basic functionality includes querying for running VMs, performing
power operations, and taking snapshots. The best aspects of it, however, lie in
being able to control operations inside of guests such as running a configuration
script. For more information on the VIX API, please refer to the product
website: http://www.vmware.com/support/developer/vix-api/.

The goal of VIX Java is to make the VIX API accessible to Java programmers. At
the moment, VIX bindings are only available for C and Perl. In addition, the 
project aims to make VIX easier to use while also allowing for object-oriented
programming. For example, a VixHandle for a VM is its own subtype (VixVmHandle).
This class contains methods pertinent to VMs, such as VixVm_LoginInGuest() which
has been renamed to loginInGuest().

HOW DOES IT WORK?

In order to access the compiled libraries that are delivered with VIX, Java 
Native Access (JNA) is used to interface with the underlying libraries. This
eliminates the complexities of JNI and C glue code, allowing for a pure Java 
solution.

This, of course, also relies on the VIX libraries being present on the machine
executing VIX Java code. The libraries are part of the VMware Workstation and
Server installation, but can also be obtained in a standalone installer from
the VIX API website: http://www.vmware.com/support/developer/vix-api/.

GETTING STARTED

Obtain jna.jar from the JNA website (https://jna.dev.java.net/). In order to
find the VIX libraries, the library path must be added to either the System 
PATH variable on Windows or the LD_LIBRARY_PATH on Linux. It is also possible
to specify the path in the system property "jna.library.path."

IMPORTANT LINKS

VIX API Website: http://www.vmware.com/support/developer/vix-api/
JNA Project Website: https://jna.dev.java.net/

LICENSE INFORMATION

Copyright (c) 2009 VMware, Inc. licensed under the terms of the BSD. All
other rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
 - Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

 - Neither the name of VMware, Inc. nor the names of its contributors may be
   used to endorse or promote products derived from this software without
   specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL VMWARE, INC. OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.