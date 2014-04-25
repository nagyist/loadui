//
// Copyright 2013 SmartBear Software
//
// Licensed under the EUPL, Version 1.1 or - as soon they will be approved by the European Commission - subsequent
// versions of the EUPL (the "Licence");
// You may not use this work except in compliance with the Licence.
// You may obtain a copy of the Licence at:
//
// http://ec.europa.eu/idabc/eupl
//
// Unless required by applicable law or agreed to in writing, software distributed under the Licence is
// distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
// express or implied. See the Licence for the specific language governing permissions and limitations
// under the Licence.
//

/**
 * Tabulates incoming messages and creates a csv output
 *
 * @id com.eviware.TableLog
 * @help http://www.loadui.org/Output/table-log-component.html
 * @name Table Log
 * @category output
 * @dependency net.sf.opencsv:opencsv:2.3
 * @nonBlocking true
 */

import au.com.bytecode.opencsv.CSVWriter
import java.io.FileWriter
import java.io.FileOutputStream
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit
import com.eviware.loadui.LoadUI

inputTerminal.description = 'Messages sent here will be displayed in the table.'
likes( inputTerminal ) { true }

table = null
tableWriterFuture = null
tableWriterDelay = 250
final messageQueue = [] as LinkedList
writer = null
writerLock = new Object()
final writeQueue = [] as ConcurrentLinkedQueue

createProperty( 'maxRows', Long, 250 )
createProperty( 'logFilePath', String )
createProperty( 'saveFile', Boolean, false )
createProperty( 'follow', Boolean, false )
createProperty( 'enabledInDistMode', Boolean, false )
createProperty( 'summaryRows', Long, 0 )
createProperty( 'appendSaveFile', Boolean, false )
createProperty( 'formatTimestamps', Boolean, true )
createProperty( 'addHeaders', Boolean, false )

cellFactory = LoadUI.headless ? null : javaFxCallback { val -> observableValue( { val.value[val.tableColumn.text] } ) }
rebuildTable = { table = tableView( prefHeight: 200, minWidth: 500 ) }
final tableColumns = [] as CopyOnWriteArraySet
final addedColumns = []
def latestHeader
saveFileName = null
def format = new SimpleDateFormat( "HH:mm:ss:SSS" )

onMessage = { o, i, m ->
	if( controller && i == remoteTerminal ) {
		//controller received message from agent
		m["Source"] = o.label
		output( m )
	}
}

output = { message ->
	def writeLog = saveFile.value && saveFileName
	if( controller || writeLog ) {
		synchronized( this ) {
			addedColumns += message.keySet() - tableColumns
			tableColumns += addedColumns
		}

		if ( formatTimestamps.value ) {
			message.each() { key, value ->
				if ( key.toLowerCase().contains("timestamp") ) {
					try {
						message[key] = format.format( new Date( value ) )
					} catch ( IllegalArgumentException e ) {
						log.info( "Failed to format Timestamp in a column whose name hinted about it containing a Timestamp" )
					}
				}
			}
		}

		if( controller ) {
			synchronized( messageQueue ) {
				messageQueue << message
				while( messageQueue.size() > maxRows.value ) messageQueue.remove( 0 )
			}
		}

		if( writeLog ) {
			putMessageInWriteQueue( message )
		}
	}

	if( ! controller && enabledInDistMode.value ) {
		// on agent and enabled, so send message to controller
		send( controllerTerminal, message )
	}
}

putMessageInWriteQueue = { message ->
	def header = tableColumns as String[]
	if( addHeaders.value && !Arrays.equals( latestHeader, header ) ) {
		writeQueue << header
		latestHeader = header
	}
	def entries = header.collect { message[it] ?: "" } as String[]
	
	writeQueue << entries
}

duringPhase( "START" ) {
	if(controller)
	{
		buildFileName()
		startTableWriter()
	}
}

duringPhase( "STOP" ) {
	if(controller)
	{
 		stopTableWriter()
 	}
 }

onAction( "COMPLETE" ) { closeWriter() }

onAction( "RESET" ) {
	buildFileName()
	log.info("RESET CLEARING")
	tableColumns.clear()
	refreshLayout()
}

onRelease = {
	stopTableWriter()
	closeWriter()
}

closeWriter = {
	withFileWriter( false ) { // TODO: Why do we do this?
		writer?.close()
		writer = null
	}
}

buildFileName = {
	if( !saveFile.value ) {
		closeWriter()
		return
	}
	
	synchronized( writerLock ) {
		if( writer ) return
	}
	
	def filePath = "${getBaseLogDir()}${File.separator}${logFilePath.value}"
	if( !validateLogFilePath( filePath ) ) {
		filePath = "${getBaseLogDir()}${File.separator}logs${File.separator}table-log${File.separator}${getDefaultLogFileName()}"
		log.warn( "Log file path wasn't specified properly. Try default path: [$filePath]" )
		if( !validateLogFilePath( filePath ) ) {
			log.error("Path: [$filePath] can't be used either. Table log component name contains invalid characters. Log file won't be saved.")
			saveFileName = null
			return
		}
	}
	if( !appendSaveFile.value ) {
		def f = new File( filePath )
		filePath = "${f.parent}${File.separator}${addTimestampToFileName( f.name )}"
	}
	new File( filePath ).parentFile.mkdirs()
	saveFileName = filePath
}

synchronized startTableWriter() {
	if ( controller && !tableWriterFuture )
		tableWriterFuture = scheduleAtFixedRate( tableWriter, tableWriterDelay, tableWriterDelay, TimeUnit.MILLISECONDS )
}

void stopTableWriter() {
	tableWriterFuture?.cancel( true )
    if ( controller ) tableWriter.run()
	tableWriterFuture = null
}

tableWriter = {
	def newColumns = []
	synchronized( this ) {
		for ( iter = addedColumns.iterator(); iter.hasNext();) {
			def added = iter.next()
			iter.remove()
			log.info "Adding column to Table Log: $added"
			def column = tableColumn( cellValueFactory: cellFactory, text: added, sortable: false )
            column.widthProperty().addListener( changeListener { obs, oldVal, width -> setAttribute( "width_$added", "$width" ) } )
            newColumns << column
			try {
				column.width = Double.parseDouble( getAttribute( "width_$added", null ) )
			} catch( e ) {
			}
		}	
	}
	
	def newMessages = []
	def excessItems =  0
	synchronized( messageQueue ) {
		excessItems = ( table.items.size() + messageQueue.size() - maxRows.value ) as int
		newMessages += messageQueue
		messageQueue.clear()
	}
	
	if ( newMessages || excessItems || newColumns ) {
        inJavaFxThread {
			if ( newColumns ) table.columns.addAll newColumns
			if ( excessItems > 0 ) table.items.remove( 0, excessItems )
			if ( newMessages ) table.items.addAll newMessages
		}
	}
	
	writeToFile()
}

writeToFile =
{
	if( saveFileName )
	{
		withFileWriter() { fileWriter -> 
			while( !writeQueue.isEmpty() )
			{
				fileWriter.writeNext( writeQueue.poll() )
			}
		}
	}
}

getBaseLogDir = { System.getProperty( 'loadui.home', '.' ) }
getDefaultLogFileName = { getLabel().replaceAll( ' ','' ) }

validateLogFilePath = { filePath ->
	try {
		// the only good way to check if file path
		// is correct is to try read and writing
		def temp = new File( filePath )
		temp.parentFile.mkdirs()
		if( !temp.exists() ) {
			def fos = new FileOutputStream( temp )
			fos.write( [0] )
			fos.close()
			temp.delete()
		} else {
			def fis = new FileInputStream( temp )
			fis.read()
			fis.close()
		}
		return true
	} catch( e ) {
		return false
	}
}

addTimestampToFileName = { it.replaceAll('^(.*?)(\\.\\w+)?$', '$1-'+System.currentTimeMillis()+'$2') }

refreshLayout = {
    if ( LoadUI.headless ) return;
	rebuildTable()
	layout(layout: 'wrap 4') {
		node( component: table, constraints: 'span' )
		action( label: 'Reset', action: { table.items.clear() } )
		action( label: 'Clear', action: {
			tableColumns.clear()
			refreshLayout()
		} )
		action( label: 'Save', action: {
			def fileChooser = fileChooser( ['CSV', '*.csv'],  title: 'Save log' )
			def saveFile = fileChooser.showSaveDialog( table.scene.window )
			if( saveFile ) {
				def flushWriter = null
				try {
					flushWriter = new CSVWriter( new FileWriter( saveFile, false ), (char) ',' )
					flushWriter.writeNext( tableColumns as String[] )
					table.items.each { message -> flushWriter.writeNext( tableColumns.collect { message[it] ?: "" } as String[] ) }
				} catch ( e ) {
					log.error( 'Failed writing log to file!', e )
				} finally {
					flushWriter?.close()
				}
			}
		} )
		property( property: enabledInDistMode, label: 'Enabled in distributed mode', constraints: 'aligny center, alignx right' )
	}
	compactLayout {
		box( widget: 'display' ) {
			node( label: 'Rows', content: { table.items.size() } )
			node( label: 'Output File', content: { saveFileName ?: '-' } )
		}
	}
}
refreshLayout()

void withFileWriter( createIfNull = true, closure ) {
	synchronized( writerLock ) {
		if( createIfNull && !writer ) {
			log.info "Creating new CSVWriter writing to $saveFileName"
			writer = new CSVWriter( new FileWriter( saveFileName, appendSaveFile.value ), ',' as char )
		}
		closure( writer )
	}
}


settings( label: "General" ) {
	box {
		property( property: maxRows, label: 'Max Rows in Table' )
	}
	//FIXME summary report not working, see generateSummary below
	//box {
	//	property( property: summaryRows, label: 'Max Rows in Summary' )
	//}
}

settings( label:'Logging' ) {
	box {
		property( property: saveFile, label: 'Save Logs' )
		property( property: logFilePath, label: 'File name (relative to the .loadui directory)' )
		property( property: appendSaveFile, label: 'Append selected file' )
		property( property: formatTimestamps, label: 'Format timestamps' )
		property( property: addHeaders, label: 'Include headers' )
		label( '(If not appending file, its name will be used to generate new log files each time test is run.)' )
	}
}

generateSummary = { chapter ->
	//FIXME this is not working... the method is called by the report creator, but the table model below is no longer working
	if( summaryRows.value > 0 ) {
		int nRows = summaryRows.value
		def rows = table.items.subList( table.items.size() - nRows, table.items.size() )
		def cols = tableColumns as List
		chapter.addSection( getLabel() ).addTable( getLabel(), new javax.swing.table.AbstractTableModel() {
			int getColumnCount() { cols.size() }
			int getRowCount() { nRows }
			String getColumnName( int c ) { cols[c] }
			Object getValueAt( int r, int c ) { rows[r][cols[c]] }
		} )
	}
}
