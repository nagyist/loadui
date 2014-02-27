/*
 * Copyright 2013 SmartBear Software
 * 
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * http://ec.europa.eu/idabc/eupl
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 */
package com.eviware.loadui.impl.reporting;

import com.eviware.loadui.api.reporting.ReportingManager;
import com.eviware.loadui.api.statistics.model.StatisticPage;
import com.eviware.loadui.api.statistics.store.Execution;
import com.eviware.loadui.api.summary.Summary;
import com.eviware.loadui.impl.reporting.statistics.ExecutionDataSource;
import com.eviware.loadui.impl.reporting.summary.SummaryDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collection;
import java.util.Map;

public class ReportingManagerImpl implements ReportingManager
{
	static final Logger log = LoggerFactory.getLogger( ReportingManagerImpl.class );
	private static final String SUMMARY_REPORT = "SummaryReport";
	private static final String RESULTS_REPORT = "ResultsReport";
	private final ReportEngine reportEngine = new ReportEngine();

	private static JasperPrint getJpFromFile( File file )
	{
		try(ObjectInputStream ois = new ObjectInputStream( new FileInputStream( file ) ))
		{
			return ( JasperPrint )ois.readObject();
		}
		catch( IOException e )
		{
			log.warn( "Problem reading Jasper file", e );
		}
		catch( ClassNotFoundException e )
		{
			log.warn( "The class of the Object in file {} cannot be found", file.getAbsolutePath() );
		}
		return null;
	}

	@Override
	public void createReport( @Nullable Summary summary, File file, String format )
	{
		if( summary == null )
			log.warn( "Summary not provided, cannot create report" );
		else try
		{
			reportEngine.generateJasperReport( new SummaryDataSource( summary ), SUMMARY_REPORT, file, format );
		}
		catch( JRException e )
		{
			log.error( "Problem creating report", e );
		}
	}

	@Override
	public void createReport( String label, Execution execution, Collection<StatisticPage> pages,
									  Map<?, Image> charts, File jpFileToPrepend )
	{
		createReport( label, execution, pages, charts, getJpFromFile( jpFileToPrepend ) );
	}

	private void createReport( String label, Execution execution, Collection<StatisticPage> pages,
									  Map<?, Image> charts, JasperPrint jpToPrepend )
	{
		reportEngine.generateJasperReport( new ExecutionDataSource( label, execution, pages, charts ), RESULTS_REPORT,
				execution.getLabel(), jpToPrepend );
	}

	@Override
	public void createReport( String label, Execution execution, Collection<StatisticPage> pages,
									  Map<?, Image> charts, File file, String format, File jpFileToPrepend )
	{
		createReport( label, execution, pages, charts, file, format, getJpFromFile( jpFileToPrepend ) );
	}

	private void createReport( String label, Execution execution, Collection<StatisticPage> pages,
									  Map<?, Image> charts, File file, String format, JasperPrint jpToPrepend )
	{
		try
		{
			reportEngine.generateJasperReport( new ExecutionDataSource( label, execution, pages, charts ), RESULTS_REPORT,
					file, format, jpToPrepend );
		}
		catch( JRException e )
		{
			e.printStackTrace();
		}
	}
}
