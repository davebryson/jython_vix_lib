/*******************************************************************************
 * Copyright (c) 2009 VMware, Inc. licensed under the terms of the BSD. All
 * other rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * - Neither the name of VMware, Inc. nor the names of its contributors may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL VMWARE, INC. OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package com.vmware.vix;

/**
 * Class representing a VIX connection to a vSphere host (vCenter or ESX).
 */
public class VixVSphereHandle extends VixHostHandle {

   /**
    * The default HTTPS port on the vSphere host.
    */
   public static final int DEFAULT_PORT = 443;

   /**
    * Constructor.
    */
   public VixVSphereHandle() {
      super();
   }

   /**
    * Constructor for a vSphere VIX host handle. Establishes a connection with
    * the given host.
    *
    * @param hostName
    *           hostname/IP address to connect to
    * @param hostPort
    *           port on the host
    * @param userName
    *           user on the host
    * @param password
    *           password for user
    * @throws VixException
    *            If there was a problem connecting with the host
    */
   public VixVSphereHandle(String hostName,
                           int hostPort,
                           String userName,
                           String password) throws VixException {
      super(VixConstants.VIX_API_VERSION,
            VixServiceProvider.VIX_SERVICEPROVIDER_VMWARE_VI_SERVER,
            "https://" + hostName + ":" + hostPort + "/sdk",
            0,
            userName,
            password);
   }

   /**
    * Constructor for a vSphere VIX host handle. Uses {@link DEFAULT_PORT} for
    * the connection.
    *
    * @param hostName
    *           hostname/IP address to connect to
    * @param userName
    *           user on the host
    * @param password
    *           password for user
    * @throws VixException
    *           If there was a problem connecting with the host
    */
   public VixVSphereHandle(String hostName,
                           String userName,
                           String password) throws VixException {
      this(hostName, DEFAULT_PORT, userName, password);
   }
}