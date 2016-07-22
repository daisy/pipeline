/**
 *   Jobs are executable units which offer a richer context than executing a XProcPipeline by itself. This package offers functionalities like creating new jobs, querying for job status, 
 *   deleting jobs or getting the messages produced during the pipeline execution. 
 *   
 *   When creating jobs the xproc pipelines input/output operations are automatically managed creating and refactoring the URIs passed to the xproc engine. 
 *   In order to get a full execution environment jobs can be created with a resource context which contains any extra file needed during the pipeline execution (e.g image files, extra css files, etc..)     
 */
package org.daisy.pipeline.job;
