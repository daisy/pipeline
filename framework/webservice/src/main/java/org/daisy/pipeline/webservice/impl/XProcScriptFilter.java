package org.daisy.pipeline.webservice.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;

import org.daisy.common.xproc.XProcOptionInfo;
import org.daisy.common.xproc.XProcPipelineInfo;
import org.daisy.common.xproc.XProcPortInfo;
import org.daisy.pipeline.script.XProcOptionMetadata;
import org.daisy.pipeline.script.XProcOptionMetadata.Output;
import org.daisy.pipeline.script.XProcScript;

public final class XProcScriptFilter {

	/** The constant ANY_URI_TYPE. */
	private static final String ANY_URI_TYPE = "anyURI";

	/** The constant ANY_FILE_URI_TYPE. */
	private static final String ANY_FILE_URI_TYPE = "anyFileURI";

	/** The constant ANY_DIR_URI_TYPE. */
	private static final String ANY_DIR_URI_TYPE = "anyDirURI";

	/*
	 * Not instantiatable
	 */
	private XProcScriptFilter() {}

	/**
	 * Filter out the outputs and options that are not relevant for the user interface, namely all
	 * the outputs and the output options (options annotated with px:output="result|temp").
	 */
	public static XProcScript withoutOutputs(XProcScript script) {
		XProcPipelineInfo xproc = script.getXProcPipelineInfo();
		// create the script builder
		XProcScript.Builder scriptBuilder = new XProcScript.Builder()
			.withShortName(script.getName())
			.withDescription(script.getDescription())
			.withHomepage(script.getHomepage())
			.withDescriptor(script.getDescriptor());
		// create the filtered pipeline info
		XProcPipelineInfo.Builder xprocBuilder = new XProcPipelineInfo.Builder();
		xprocBuilder.withURI(xproc.getURI());
		//copy filesets
		for (String fileset: script.getInputFilesets()) {
			scriptBuilder.withInputFileset(fileset);
		}
		for (String fileset: script.getOutputFilesets()) {
			scriptBuilder.withOutputFileset(fileset);
		}
		// copy input ports
		for (XProcPortInfo port : xproc.getInputPorts()) {
			xprocBuilder.withPort(port);
			scriptBuilder.withPortMetadata(port.getName(), script.getPortMetadata(port.getName()));
		}
		// copy parameter ports
		for (String port : xproc.getParameterPorts()) {
			// FIXME parameter ports should return XProcPortInfo
			xprocBuilder.withPort(XProcPortInfo.newParameterPort(port, false));
			scriptBuilder.withPortMetadata(port, script.getPortMetadata(port));
		}
		// output ports are not copied
		// copy options
		for (XProcOptionInfo option : xproc.getOptions()) {
			XProcOptionMetadata metadata = script.getOptionMetadata(option.getName());
			// filter-out options that are both OUTPUT options with type
			// inheriting from anyURI
			if (!(metadata.getOutput() != Output.NA &&
					(ANY_URI_TYPE.equals(metadata.getMediaType())
					 ||
					 ANY_FILE_URI_TYPE.equals(metadata.getType())
					 ||
					 ANY_DIR_URI_TYPE.equals(metadata.getType())))) {
				xprocBuilder.withOption(option);
				scriptBuilder.withOptionMetadata(option.getName(), metadata);
			}

		}
		scriptBuilder.withPipelineInfo(xprocBuilder.build());
		return scriptBuilder.build();
	}

	/**
	 * Remove namespaces from options and rename if needed (if multiple options with the same local
	 * part). This is required for clients that are not namespace aware.
	 */
	public static XProcScript renameOptions(XProcScript script) {
		XProcPipelineInfo xproc = script.getXProcPipelineInfo();
		XProcScript.Builder scriptBuilder = new XProcScript.Builder()
			.withShortName(script.getName())
			.withDescription(script.getDescription())
			.withHomepage(script.getHomepage())
			.withDescriptor(script.getDescriptor());
		XProcPipelineInfo.Builder xprocBuilder = new XProcPipelineInfo.Builder();
		xprocBuilder.withURI(xproc.getURI());
		for (String fileset: script.getInputFilesets()) {
			scriptBuilder.withInputFileset(fileset);
		}
		for (String fileset: script.getOutputFilesets()) {
			scriptBuilder.withOutputFileset(fileset);
		}
		for (XProcPortInfo port : xproc.getInputPorts()) {
			xprocBuilder.withPort(port);
			scriptBuilder.withPortMetadata(port.getName(), script.getPortMetadata(port.getName()));
		}
		for (String port : xproc.getParameterPorts()) {
			xprocBuilder.withPort(XProcPortInfo.newParameterPort(port, false));
			scriptBuilder.withPortMetadata(port, script.getPortMetadata(port));
		}
		Map<QName,QName> rename = renameOptions(xproc.getOptions());
		for (XProcOptionInfo option : xproc.getOptions()) {
			QName oldName = option.getName();
			QName newName = rename.get(oldName);
			xprocBuilder.withOption(
				XProcOptionInfo.newOption(newName, option.isRequired(), option.getSelect()));
			scriptBuilder.withOptionMetadata(newName, script.getOptionMetadata(oldName));
		}
		scriptBuilder.withPipelineInfo(xprocBuilder.build());
		return scriptBuilder.build();
	}

	public static Map<QName,QName> renameOptions(Iterable<XProcOptionInfo> options) {
		List<QName> newNames = new ArrayList<>();
		List<QName> collisions = new ArrayList<>();
		for (XProcOptionInfo o : options) {
			QName oldName = o.getName();
			QName newName = new QName(oldName.getLocalPart());
			if (newNames.contains(newName))
				collisions.add(newName);
			newNames.add(newName);
		}
		for (QName n : collisions) {
			ListIterator<QName> names = newNames.listIterator();
			int i = 1;
			while (names.hasNext())
				if (names.next().equals(n))
					names.set(new QName(n.getLocalPart() + "-" + i++));
		}
		Map<QName,QName> map = new HashMap<>();
		int i = 0;
		for (XProcOptionInfo o : options) {
			QName oldName = o.getName();
			map.put(oldName, newNames.get(i++));
		}
		return map;
	}
}
