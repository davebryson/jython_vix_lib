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

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * Wrapper for VIX calls that looks as close as possible to the API reference.
 * <p/>
 * Some data types may appear differently in this wrapper. That is because JNA
 * needs to know how to interface with the C library, and data types like
 * VixHandle are actually int values.
 * <p/>
 * Based on the VIX C library version 1.8.1
 * {@link http://www.vmware.com/support/developer/vix-api/vix18_reference/}
 */
public interface VixLibrary extends Library {

   /**
    * Object that allows us to load and access the VIX library.
    */
   public static VixLibrary INSTANCE =
         (VixLibrary) Native.loadLibrary("vixAllProducts", VixLibrary.class);

   /**
    * Creates a new host handle. This handle cannot be shared or reused after
    * disconnect.
    *
    * @param apiVersion
    *           Version of the VIX API to use.
    * @param hostType
    *           VixServiceProvider
    * @param hostName
    *           Hostname of the server to connect to
    * @param hostPort
    *           Network port
    * @param userName
    * @param password
    * @param options
    * @param propertyListHandle
    *           Must be VIX_INVALID_HANDLE
    * @param callbackProc
    * @param clientData
    * @return a new VixHandle
    */
   public VixHandle VixHost_Connect(
         int apiVersion,
         VixServiceProvider hostType,
         String hostName,
         int hostPort,
         String userName,
         String password,
         VixHostOptions options,
         VixHandle propertyListHandle,
         VixEventProc callbackProc, // VixEventProc
         Pointer clientData);

   /**
    * Disconnect from the host.
    *
    * @param hostHandle
    *           VixHandle for a host
    */
   public void VixHost_Disconnect(VixHandle hostHandle);

   /**
    * This function asynchronously finds Vix objects and calls the application's
    * callback function to report each object found. For example, when used to
    * find all running virtual machines, VixHost_FindItems() returns a series of
    * virtual machine file path names.
    *
    * @param hostHandle
    *           VixHandle for a host
    * @param searchType
    *           VixFindItemType
    * @param searchCriteria
    *           Must be VIX_INVALID_HANDLE
    * @param timeout
    *           Must be -1
    * @param callbackProc
    *           Function to be invoked when this completes
    * @param clientData
    *           Data to be passed to the callback function
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous call.
    */
   public VixHandle VixHost_FindItems(
         VixHandle hostHandle,
         VixFindItemType searchType,
         VixHandle searchCriteria, // Must be VIX_INVALID_HANDLE (0)
         int timeout,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function opens a virtual machine on the host that is identified by
    * the hostHandle parameter and returns a context to that machine as a
    * virtual machine handle. This function supercedes VixVM_Open().
    *
    * @param hostHandle
    *           The handle of a host object, typically returned from
    *           VixHost_Connect().
    * @param vmxFilePathName
    *           The path name of the virtual machine configuration file on the
    *           local host.
    * @param options
    *           Must be VIX_VMOPEN_NORMAL.
    * @param propertyListHandle
    *           A handle to a property list containing extra information that
    *           might be needed to open the VM. This parameter is optional; you
    *           can pass VIX_INVALID_HANDLE if no extra information is needed.
    * @param callbackproc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc procedure.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous call.
    */
   public VixVmHandle VixHost_OpenVM(
         VixHandle hostHandle,
         String vmxFilePathName,
         VixVMOpenOptions options,
         VixHandle propertyListHandle,
         VixEventProc callbackproc,
         Pointer clientData);

   /**
    * This function adds a virtual machine to the host's inventory.
    * @param hostHandle
    *           VixHandle for a host
    * @param vmxFilePath
    * @param callbackProc
    *           Function to be invoked when this completes
    * @param clientData
    *           Data to be passed to the callback function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous call.
    */
   public VixHandle VixHost_RegisterVM(
         VixHandle hostHandle,
         String vmxFilePath,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * Removes a VM from the host's inventory.
    *
    * @param hostHandle
    * @param vmxFilePath
    * @param callbackProc
    *           Function to be invoked when the operation completes.
    * @param clientData
    *           Data to be passed to the callback function.
    * @return Job handle for this asynchronous call.
    */
   public VixHandle VixHost_UnregisterVM(
         VixHandle hostHandle,
         String vmxFilePath,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function performs a non-blocking test for completion of an
    * asynchronous operation. It can be used to poll for completion in
    * situations where a blocking function or a callback function is not
    * desirable.
    *
    * @param jobHandle
    *           VixHandle for a job
    * @param complete
    *           Pointer for a boolean (IntByReference). This value will be set
    *           to 1 if the job has completed.
    * @return VixError code. VIX_OK if successful.
    */
   public VixError VixJob_CheckCompletion(
         VixHandle jobHandle,
         IntByReference complete);

   /**
    * Retrieves the error code from a job that has completed. Applications
    * should call this function only after an asynchronous job has completed.
    * The error code returned by this function is the same as the error code
    * returned by VixJob_Wait().
    *
    * @param jobHandle
    *           The handle of a job object, returned from any asynchronous Vix
    *           function.
    * @return VixError. The result returned by a completed asynchronous
    *         function.
    */
   public VixError VixJob_GetError(VixHandle jobHandle);

   /**
    * Retrieves the property at a specific index in a list. You can use this to
    * iterate through returned property lists.
    *
    * @param jobHandle
    *           The handle of a job object, returned from any asynchronous Vix
    *           function.
    * @param index
    *           Index into the property list of the job object.
    * @param propertyID
    *           A property ID.
    * @param args
    *           List of additional properties and pointers to objects that will
    *           have their values set to that of the property.
    * @return VixError. The result returned by a completed asynchronous
    *         function.
    */
   public VixError VixJob_GetNthProperties(
         VixHandle jobHandle,
         int index,
         VixPropertyID propertyID,
         Object... args);

   /**
    * Retrieves the number of instances of the specified property. Used to work
    * with returned property lists.
    *
    * @param jobHandle
    *           The handle of a job object, returned from any asynchronous Vix
    *           function.
    * @param resultPropertyID
    *           A property ID.
    * @return int. The number of properties with an ID of resultPropertyID.
    */
   public int VixJob_GetNumProperties(
         VixHandle jobHandle,
         VixPropertyID resultPropertyID);

   /**
    * Wait for a particular job to complete.
    *
    * @param vixHandle
    *           The handle of a job object, returned from any asynchronous Vix
    *           function.
    * @param propertyID
    *           The identity of a property, or else VIX_PROPERTY_NONE.
    * @param moreProperties
    *           List of pairs of property IDs and Pointer objects to set values
    *           for. Must be terminated by VIX_PROPERTY_NONE.
    * @return VixError. The error resulting from the asynchronous operation that
    *         returned the job handle.
    */
   public VixError VixJob_Wait(
         VixHandle vixHandle,
         VixPropertyID propertyID,
         Object... moreProperties);

   /**
    * This function creates a new VIX_HANDLETYPE_PROPERTY_LIST handle with a set
    * of properties on it. VIX_HANDLETYPE_PROPERTY_LIST handles are used to pass
    * extra arguments to many functions.
    *
    * @param vixHandle
    *           Any valid handle.
    * @param resultHandle
    *           (output) A handle to the new property list.
    * @param firstPropertyID
    *           A property ID. See below for valid values.
    * @param moreProperties
    *           List of addtional properties and Pointer objects to set values
    *           for. Must be terminated by VIX_PROPERTY_NONE.
    * @return VixError. This function returns VIX_OK if it succeeded, otherwise
    *         the return value indicates an error.
    */
   public VixError VixPropertyList_AllocPropertyList(
         VixHandle vixHandle,
         VixHandleByReference resultHandle, // VixHandle *resultHandle
         VixPropertyID firstPropertyID,
         Object... moreProperties);

   /**
    * This function returns the specified child snapshot.
    *
    * @param parentSnapshotHandle
    *           A snapshot handle.
    * @param index
    *           Index into the list of snapshots.
    * @param childSnapshotHandle
    *           (output) A handle reference to the child snapshot.
    * @return VixError
    */
   public VixError VixSnapshot_GetChild(
         VixHandle parentSnapshotHandle,
         int index,
         VixHandleByReference childSnapshotHandle);

   /**
    * This function returns the number of child snapshots of a specified
    * snapshot.
    *
    * @param parentSnapshotHandle
    *           A snapshot handle.
    * @param numChildSnapshots
    *           (output) The number of child snapshots belonging to the
    *           specified snapshot.
    * @return VixError
    */
   public VixError VixSnapshot_GetNumChildren(
         VixHandle parentSnapshotHandle,
         IntByReference numChildSnapshots);

   /**
    * This function returns the parent of a snapshot.
    *
    * @param snapshotHandle
    *           A snapshot handle.
    * @param parentSnapshotHandle
    *           (output) A handle to the parent of the specified snapshot.
    * @return VixError
    */
   public VixError VixSnapshot_GetParent(
         VixHandle snapshotHandle,
         VixHandleByReference parentSnapshotHandle);

   /**
    * This function mounts a new shared folder in the virtual machine.
    * <p/>
    * Workstation only.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param shareName
    *           Specifies the guest path name of the new shared folder.
    * @param hostPathName
    *           Specifies the host path of the shared folder.
    * @param flags
    *           The folder options. VixMsgSharedFolderOptions.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_AddSharedFodler(
         VixHandle vmHandle,
         String shareName,
         String hostPathName,
         int flags,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function records a virtual machine's activity as a snapshot object.
    * The handle of the snapshot object is returned in the job object's
    * properties.
    * <p/>
    * Workstation only.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param name
    *           A user-defined name for the recording; need not be unique.
    * @param description
    *           A user-defined description for the recording.
    * @param options
    *           Must be zero.
    * @param propertyListHandle
    *           Must be VIX_INVALID_HANDLE.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_BeginRecording(
         VixHandle vmHandle,
         String name,
         String description,
         int options,
         VixHandle propertyListHandle,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function replays a recording of a virtual machine.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param snapshotHandle
    *           snapshot handle that represents the recording.
    * @param options
    *           Must be VIX_VMPOWEROP_NORMAL or VIX_VMPOWEROP_LAUNCH_GUI.
    * @param propertyListHandle
    *           Must be VIX_INVALID_HANDLE.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_BeginReplay(
         VixHandle vmHandle,
         VixHandle snapshotHandle,
         int options,
         VixHandle propertyListHandle,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function captures the screen of the guest operating system.
    * <p/>
    * May be workstation only.
    *
    * @param vmHandle
    *           The handle to the VM.
    * @param captureType
    *           the data format. Must be VIX_CAPTURESCREENFORMAT_PNG
    * @param additionalProperties
    *           VixHandle. Must be VIX_INVALID_HANDLE.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_CaptureScreenImage(
         VixHandle vmHandle,
         int captureType,
         VixHandle additionalProperties,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * Creates a copy of the virtual machine specified by the 'vmHandle'
    * parameter.
    *
    * @param vmHandle
    *           Identifies a virtual machine, which is referred to as the
    *           clone's parent. Call VixVM_Open() to create a virtual machine
    *           handle.
    * @param snapshotHandle
    *           Optional. A snapshot belonging to the virtual machine specified
    *           by the 'vmHandle' parameter. If you pass VIX_INVALID_HANDLE, the
    *           clone will be based off the current state of the virtual
    *           machine. If you pass a valid snapshot handle, the clone will be
    *           a copy of the state of the virtual machine at the time the
    *           snapshot was taken.
    * @param cloneType
    *           Must be either VIX_CLONETYPE_FULL or VIX_CLONETYPE_LINKED.
    * @param destConfigPathName
    *           The path name of the virtual machine configuration file that
    *           will be created for the virtual machine clone produced by this
    *           operation. This should be a full absolute path name, with
    *           directory names delineated according to host system convention:
    *           \ for Windows and / for Linux.
    * @param options
    *           Must be 0.
    * @param propertyListHandle
    *           Must be VIX_INVALID_HANDLE.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_Clone(
         VixHandle vmHandle,
         VixHandle snapshotHandle,
         VixCloneType cloneType,
         String destConfigPathName,
         int options,
         VixHandle propertyListHandle,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * Copies a file or directory from the guest operating system to the local
    * system (where the Vix client is running).
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param guestPathName
    *           The path name of a file on a file system available to the guest.
    * @param hostPathName
    *           The path name of a file on a file system available to the Vix
    *           client.
    * @param options
    *           Must be 0.
    * @param propertyListHandle
    *           Must be VIX_INVALID_HANDLE.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_CopyFileFromGuestToHost(
         VixHandle vmHandle,
         String guestPathName,
         String hostPathName,
         int options,
         VixHandle propertyListHandle,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * Copies a file or directory from the local system (where the Vix client is
    * running) to the guest operating system.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param hostPathName
    *           The path name of a file on a file system available to the Vix
    *           client.
    * @param guestPathName
    *           The path name of a file on a file system available to the guest.
    * @param options
    *           Must be 0.
    * @param propertyListHandle
    *           Must be VIX_INVALID_HANDLE.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_CopyFileFromHostToGuest(
         VixHandle vmHandle,
         String hostPathName,
         String guestPathName,
         int options,
         VixHandle propertyListHandle,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function creates a directory in the guest operating system.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param pathName
    *           The path to the directory to be created.
    * @param propertyListHandle
    *           Must be VIX_INVALID_HANDLE.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_CreateDirectoryInGuest(
         VixHandle vmHandle,
         String pathName,
         VixHandle propertyListHandle,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function saves a copy of the virtual machine state as a snapshot
    * object. The handle of the snapshot object is returned in the job object
    * properties.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param name
    *           A user-defined name for the snapshot; need not be unique.
    * @param description
    *           A user-defined description for the snapshot.
    * @param options
    *           Flags to specify how the snapshot should be created. Any
    *           combination of the following or 0:
    *           <ul>
    *           <li>VIX_SNAPSHOT_INCLUDE_MEMORY - Captures the full state of a
    *           running virtual machine, including the memory.</li>
    *           </ul>
    * @param propertyListHandle
    *           Must be VIX_INVALID_HANDLE.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_CreateSnapshot(
         VixHandle vmHandle,
         String name,
         String description,
         int options,
         VixHandle propertyListHandle,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function creates a temporary file in the guest operating system.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param options
    *           Must be 0.
    * @param propertyListHandle
    *           Must be VIX_INVALID_HANDLE.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_CreateTempFileInGuest(
         VixHandle vmHandle,
         int options,
         VixHandle propertyListHandle,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function permanently deletes a virtual machine from your host system.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param deleteOptions
    *           For VMware Server 2.0 and ESX, this value must be
    *           VIX_VMDELETE_DISK_FILES. For all other versions it can be either
    *           0 or VIX_VMDELETE_DISK_FILES. When option is
    *           VIX_VMDELETE_DISK_FILES, deletes all associated files. When
    *           option is 0, does not delete *.vmdk virtual disk file(s).
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_Delete(
         VixHandle vmHandle,
         VixVMDeleteOptions deleteOptions,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function deletes a directory in the guest operating system. Any files
    * or subdirectories in the specified directory will also be deleted.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param pathName
    *           The path to the directory to be deleted.
    * @param options
    *           Must be 0.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_DeleteDirectoryInGuest(
         VixHandle vmHandle,
         String pathName,
         int options,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function deletes a file in the guest operating system.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param guestPathName
    *           The path to the file to be deleted.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_DeleteFileInGuest(
         VixHandle vmHandle,
         String guestPathName,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function tests the existence of a directory in the guest operating
    * system.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param pathName
    *           The path to the directory in the guest to be checked.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_DirectoryExistsInGuest(
         VixHandle vmHandle,
         String pathName,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function enables or disables all shared folders as a feature for a
    * virtual machine.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param enabled
    *           TRUE if enabling shared folders is desired. FALSE otherwise.
    * @param options
    *           Must be 0.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_EnableSharedFolders(
         VixHandle vmHandle,
         boolean enabled,
         int options,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function stops recording a virtual machine's activity.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param options
    *           Must be zero.
    * @param propertyListHandle
    *           Must be VIX_INVALID_HANDLE.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_EndRecording(
         VixHandle vmHandle,
         int options,
         VixHandle propertyListHandle,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function stops replaying a virtual machine's recording.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param options
    *           Must be zero.
    * @param propertyListHandle
    *           Must be VIX_INVALID_HANDLE.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_EndReplay(
         VixHandle vmHandle,
         int options,
         VixHandle propertyListHandle,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function tests the existence of a file in the guest operating system.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param guestPathName
    *           The path to the file to be tested.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_FileExistsInGuest(
         VixHandle vmHandle,
         String guestPathName,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function returns the handle of the current active snapshot belonging
    * to the virtual machine referenced by vmHandle.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param snapshotHandle
    *           An output parameter that receives a handle to a snapshot.
    * @return VixError.
    */
   public VixError VixVM_GetCurrentSnapshot(
         VixHandle vmHandle,
         VixHandleByReference snapshotHandle);

   /**
    * This function returns information about a file in the guest operating
    * system.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param pathname
    *           The path name of the file in the guest.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_GetFileInfoInGuest(
         VixHandle vmHandle,
         String pathname,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function gets the handle of the snapshot matching the given name
    * in the virtual machine referenced by vmHandle.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param name
    *           Unique snapshot name.
    * @param snapshotHandle
    *           Reference object that will hold the value of the handle.
    * @return VixError
    * @throws VixException
    */
   public VixError VixVM_GetNamedSnapshot(
         VixHandle vmHandle,
         String name,
         VixHandleByReference snapshotHandle);

   /**
    * This function returns the number of top-level (root) snapshots belonging
    * to a virtual machine.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param result
    *           (output) The number of root snapshots on this virtual machine.
    * @return VixError.
    */
   public VixError VixVM_GetNumRootSnapshots(
         VixHandle vmHandle,
         IntByReference result);

   /**
    * This function returns the number of shared folders mounted in the virtual
    * machine.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_GetNumSharedFolders(
         VixHandle vmHandle,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function returns the handle of the specified snapshot belonging to
    * the virtual machine referenced by vmHandle.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param index
    *           Identifies a root snapshot. See below for range of values.
    * @param snapshotHandle
    *           (output) A handle reference to a snapshot.
    * @return VixError.
    */
   public VixError VixVM_GetRootSnapshot(
         VixHandle vmHandle,
         int index,
         VixHandleByReference snapshotHandle);

   /**
    * This function returns the state of a shared folder mounted in the virtual
    * machine.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param index
    *           Identifies the shared folder.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_GetSharedFolderState(
         VixHandle vmHandle,
         int index,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * Prepares to install VMware Tools on the guest operating system.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param options
    *           Must be 0.
    * @param commandLineArgs
    *           Must be NULL.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_InstallTools(
         VixHandle vmHandle,
         int options,
         String commandLineArgs,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function terminates a process in the guest operating system.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param pid
    *           The ID of the process to be killed.
    * @param options
    *           Must be 0.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_KillProcessInGuest(
         VixHandle vmHandle,
         long pid,
         int options,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function lists a directory in the guest operating system.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param pathName
    *           The path name of a directory to be listed.
    * @param options
    *           Must be 0.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_ListDirectoryInGuest(
         VixHandle vmHandle,
         String pathName,
         int options,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function lists the running processes in the guest operating system.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param options
    *           Must be 0.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_ListProcessesInGuest(
         VixHandle vmHandle,
         int options,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function establishes a guest operating system authentication context
    * that can be used with guest functions for the given virtual machine
    * handle.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param userName
    *           The name of a user account on the guest operating system.
    * @param password
    *           The password of the account identified by userName.
    * @param options
    *           Must be 0 or VIX_LOGIN_IN_GUEST_REQUIRE_INTERACTIVE_ENVIRONMENT,
    *           which forces interactive guest login within a graphical session
    *           that is visible to the user (see below). On Linux, interactive
    *           environment requires that the X11 window system be running to
    *           start the vmware-user process. Without X11, pass 0 as options to
    *           start the vmware-guestd process instead.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_LoginInGuest(
         VixHandle vmHandle,
         String userName,
         String password,
         int options,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function removes any guest operating system authentication context
    * created by a previous call to VixVM_LoginInGuest().
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param callbackproc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_LogoutFromGuest(
         VixHandle vmHandle,
         VixEventProc callbackproc,
         Pointer clientData);

   /**
    * This function opens a virtual machine on the host that is identified by
    * the hostHandle parameter and returns a context to that machine as a
    * virtual machine handle.
    *
    * @param hostHandle
    *           The handle of a host object, typically returned from
    *           VixHost_Connect().
    * @param vmxFilePathName
    *           The path name of the virtual machine configuration file on the
    *           local host.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc procedure.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous call.
    */
   public VixHandle VixVM_Open(
         VixHandle hostHandle,
         String vmxFilePathName,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function opens a browser window on the specified URL in the guest
    * operating system.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param url
    *           The URL to be opened.
    * @param windowState
    *           Must be 0.
    * @param propertyListHandle
    *           Must be VIX_INVALID_HANDLE.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   @Deprecated
   public VixHandle VixVM_OpenUrlInGuest(
         VixHandle vmHandle,
         String url,
         int windowState,
         VixHandle propertyListHandle,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function pauses a virtual machine. See Remarks section for pause
    * behavior when used with different operations.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param options
    *           Must be zero.
    * @param propertyListHandle
    *           Must be VIX_INVALID_HANDLE.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_Pause(
         VixHandle vmHandle,
         int options,
         VixHandle propertyListHandle,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function powers off a virtual machine.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param powerOffOptions
    *           Must be VIX_VMPOWEROP_NORMAL or VIX_VMPOWEROP_FROM_GUEST.
    * @param callbackProc
    *           A callback function that will be invoked when the power
    *           operation is complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_PowerOff(
         VixHandle vmHandle,
         VixVMPowerOpOptions powerOffOptions,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * Powers on a virtual machine.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param powerOnOptions
    *           VIX_VMPOWEROP_NORMAL or VIX_VMPOWEROP_LAUNCH_GUI.
    * @param propertyListHandle
    *           Must be VIX_INVALID_HANDLE.
    * @param callbackProc
    *           A callback function that will be invoked when the power
    *           operation is complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_PowerOn(
         VixHandle vmHandle,
         VixVMPowerOpOptions powerOnOptions,
         VixHandle propertyListHandle,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function reads variables from the virtual machine state. This
    * includes the virtual machine configuration, environment variables in the
    * guest, and VMware "Guest Variables".
    * <p/>
    * The result of the call is in the property
    * VIX_PROPERTY_JOB_RESULT_VM_VARIABLE_STRING on the returning jobHandle.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param variableType
    *           The type of variable to read
    * @param name
    *           The name of the variable.
    * @param options
    *           must be 0.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_ReadVariable(
         VixHandle vmHandle,
         int variableType,
         String name,
         int options,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function removes a shared folder in the virtual machine.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param shareName
    *           Specifies the guest pathname of the shared folder to delete.
    * @param flags
    *           Must be 0.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_RemoveSharedFolder(
         VixHandle vmHandle,
         String shareName,
         int flags,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function deletes all saved states for the specified snapshot.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param snapshotHandle
    *           A handle to a snapshot. Call VixVM_GetRootSnapshot() to get a
    *           snapshot handle.
    * @param options
    *           Flags to specify optional behavior
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_RemoveSnapshot(
         VixHandle vmHandle,
         VixHandle snapshotHandle,
         int options,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function renames a file or directory in the guest operating system.
    * Blocks until the operation completes.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param oldName
    *           The path to the file to be renamed.
    * @param newName
    *           The path to the new file.
    * @param options
    *           Must be 0.
    * @param propertyListHandle
    *           Must be VIX_INVALID_HANDLE.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_RenameFileInGuest(
         VixHandle vmHandle,
         String oldName,
         String newName,
         int options,
         VixHandle propertyListHandle,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function resets a virtual machine.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param powerOnOptions
    *           Must be VIX_VMPOWEROP_NORMAL or VIX_VMPOWEROP_FROM_GUEST.
    * @param callbackProc
    *           A callback function that will be invoked when the power
    *           operation is complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_Reset(
         VixHandle vmHandle,
         VixVMPowerOpOptions powerOnOptions,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * Restores the virtual machine to the state when the specified snapshot was
    * created.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param snapshotHandle
    *           A handle to a snapshot. Call VixVM_GetRootSnapshot() to get a
    *           snapshot handle.
    * @param options
    *           Any applicable VixVMPowerOpOptions. If the virtual machine was
    *           powered on when the snapshot was created, then this will
    *           determine how the virtual machine is powered back on. To prevent
    *           the virtual machine from being powered on regardless of the
    *           power state when the snapshot was created, use the
    *           VIX_VMPOWEROP_SUPPRESS_SNAPSHOT_POWERON flag.
    *           VIX_VMPOWEROP_SUPPRESS_SNAPSHOT_POWERON is mutually exclusive to
    *           all other VixVMPowerOpOptions.
    * @param propertyListHandle
    *           Must be VIX_INVALID_HANDLE.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_RevertToSnapshot(
         VixHandle vmHandle,
         VixHandle snapshotHandle,
         int options,
         VixHandle propertyListHandle,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function runs a program in the guest operating system. The program
    * must be stored on a file system available to the guest before calling this
    * function.
    * <p/>
    * When the job completes, the following properties are available on the job
    * handle:
    * <ul>
    * <li>VIX_PROPERTY_JOB_RESULT_PROCESS_ID: the process id; however, if the
    * guest has an older version of Tools (those released with Workstation 6 and
    * earlier) and the VIX_RUNPROGRAM_RETURN_IMMEDIATELY flag is used, then the
    * process ID will not be returned from the guest and this property will not
    * be set on the job handle, so attempting to access this property will
    * result in VIX_E_UNRECOGNIZED_PROPERTY being returned;</li>
    * <li>VIX_PROPERTY_JOB_RESULT_GUEST_PROGRAM_ELAPSED_TIME: the process
    * elapsed time;</li>
    * <li>VIX_PROPERTY_JOB_RESULT_GUEST_PROGRAM_EXIT_CODE: the process exit
    * code.</li>
    * </ul>
    * If the option parameter is VIX_RUNPROGRAM_RETURN_IMMEDIATELY, the latter
    * two will both be 0.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param guestProgramName
    *           The path name of an executable file on the guest operating
    *           system.
    * @param commandLineArgs
    *           A string to be passed as command line arguments to the
    *           executable identified by guestProgramName.
    * @param options
    *           Run options for the program. See the remarks below.
    * @param propertyListHandle
    *           Must be VIX_INVALID_HANDLE.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_RunProgramInGuest(
         VixHandle vmHandle,
         String guestProgramName,
         String commandLineArgs,
         VixRunProgramOptions options,
         VixHandle propertyListHandle,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function runs a script in the guest operating system.
    * <p/>
    * When the job completes, the following properties are available on the job
    * handle:
    * <ul>
    * <li>VIX_PROPERTY_JOB_RESULT_PROCESS_ID: the process id; however, if the
    * guest has an older version of Tools (those released with Workstation 6 and
    * earlier) and the VIX_RUNPROGRAM_RETURN_IMMEDIATELY flag is used, then the
    * process ID will not be returned from the guest and this property will not
    * be set on the job handle, so attempting to access this property will
    * result in VIX_E_UNRECOGNIZED_PROPERTY being returned;</li>
    * <li>VIX_PROPERTY_JOB_RESULT_GUEST_PROGRAM_ELAPSED_TIME: the process
    * elapsed time;</li>
    * <li>VIX_PROPERTY_JOB_RESULT_GUEST_PROGRAM_EXIT_CODE: the process exit
    * code.</li>
    * </ul>
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param interpreter
    *           The path to the script interpreter, or NULL to use cmd.exe as
    *           the interpreter on Windows.
    * @param scriptText
    *           The text of the script.
    * @param options
    *           Run options for the program. See the reference.
    * @param propertyListHandle
    *           Must be VIX_INVALID_HANDLE.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_RunScriptInGuest(
         VixHandle vmHandle,
         String interpreter,
         String scriptText,
         VixRunProgramOptions options,
         VixHandle propertyListHandle,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function modifies the state of a shared folder mounted in the virtual
    * machine.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param shareName
    *           Specifies the name of the shared folder.
    * @param hostPathName
    *           Specifies the host path of the shared folder.
    * @param flags
    *           The new flag settings.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_SetSharedFolderState(
         VixHandle vmHandle,
         String shareName,
         String hostPathName,
         VixMsgSharedFolderOptions flags,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function suspends a virtual machine.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param powerOffOptions
    *           Must be 0.
    * @param callbackProc
    *           A callback function that will be invoked when the power
    *           operation is complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_Suspend(
         VixHandle vmHandle,
         VixVMPowerOpOptions powerOffOptions,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function continues execution of a paused virtual machine.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param options
    *           Must be zero.
    * @param propertyListHandle
    *           Must be VIX_INVALID_HANDLE.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_Unpause(
         VixHandle vmHandle,
         int options,
         VixHandle propertyListHandle,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * Upgrades the virtual hardware version of the virtual machine to match the
    * version of the VIX library. This has no effect if the virtual machine is
    * already at the same version or at a newer version than the VIX library.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param options
    *           Must be 0.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_UpgradeVirtualHardware(
         VixHandle vmHandle,
         int options,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function signals the job handle when VMware Tools has successfully
    * started in the guest operating system. VMware Tools is a collection of
    * services that run in the guest.
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param timeoutInSeconds
    *           The timeout in seconds. If VMware Tools has not started by this
    *           time, the function completes with an error. If the value of this
    *           argument is zero or negative, then there will be no timeout.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_WaitForToolsInGuest(
         VixHandle vmHandle,
         int timeoutInSeconds,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * This function writes variables to the virtual machine state. This includes
    * the virtual machine configuration, environment variables in the guest, and
    * VMware "Guest Variables".
    *
    * @param vmHandle
    *           Identifies a virtual machine. Call VixVM_Open() to create a
    *           virtual machine handle.
    * @param variableType
    *           The type of variable to read.
    * @param valueName
    *           The name of the variable.
    * @param value
    *           The value to be written.
    * @param options
    *           Must be 0.
    * @param callbackProc
    *           A callback function that will be invoked when the operation is
    *           complete.
    * @param clientData
    *           A parameter that will be passed to the callbackProc function.
    * @return VixHandle. A job handle that describes the state of this
    *         asynchronous operation.
    */
   public VixHandle VixVM_WriteVariable(
         VixHandle vmHandle,
         int variableType,
         String valueName,
         String value,
         int options,
         VixEventProc callbackProc,
         Pointer clientData);

   /**
    * When Vix_GetProperties() or Vix_JobWait() returns a string or blob
    * property, it allocates a buffer for the data. Client applications are
    * responsible for calling Vix_FreeBuffer() to free the buffer when no longer
    * needed.
    *
    * @param p
    *           A pointer returned by a call to Vix_GetProperties() or
    *           Vix_JobWait().
    */
   public void Vix_FreeBuffer(Pointer p);

   /**
    * Returns a human-readable string that describes the error.
    *
    * @param vixError
    *           A Vix error code returned by any other Vix function.
    * @param locale
    *           Must be NULL.
    * @return A human-readable string that describes the error.
    */
   public String Vix_GetErrorText(VixError vixError, String locale);

   /**
    * Given a handle, this returns the handle type.
    *
    * @param handle
    *           Any handle returned by a Vix function.
    * @return An enumerated type that identifies what kind of handle this is.
    */
   public VixHandleType Vix_GetHandleType(VixHandle handle);

   /**
    * This function allows you to get one or more properties from a handle. For
    * a list of property IDs, see Topics > Types > VixPropertyID.
    *
    * @param handle
    *           Any handle returned by a Vix function.
    * @param firstPropertyID
    *           A property ID. See below for valid values.
    * @param args
    *           Pairs of property id and pointers that will hold the values for
    *           that property. Must be terminated by VIX_PROPERTY_NONE.
    * @throws VixException
    */
   public VixError Vix_GetProperties(
         VixHandle handle,
         VixPropertyID firstPropertyID,
         Object... moreProps);

   /**
    * Given a property ID, this function returns the type of that property.
    *
    * @param handle
    *           Any handle returned by a VIX function.
    * @param propertyID
    *           A property ID. See below for valid values.
    * @param propertyType
    *           The type of the data stored by the property.
    * @return VixError. This function returns VIX_OK if it succeeded.
    */
   public VixError Vix_GetPropertyType(
         VixHandle handle,
         VixPropertyID propertyID,
         VixPropertyTypeByReference propertyType);

   /**
    * Vix_PumpEvents is used in single threaded applications that require the
    * Vix library to be single threaded. Tasks that would normally be executed
    * in a separate thread by the Vix library will be executed when
    * Vix_PumpEvents() is called.
    *
    * @param hostHandle
    *           The handle to the local host object.
    * @param options
    *           Must be 0.
    */
   public void Vix_PumpEvents(VixHandle hostHandle, VixPumpEventsOptions options);

   /**
    * This function decrements the reference count for a handle and destroys the
    * handle when there are no references.
    *
    * @param vixHandle
    *           Any handle returned by a Vix function.
    */
   public void Vix_ReleaseHandle(VixHandle vixHandle);
}