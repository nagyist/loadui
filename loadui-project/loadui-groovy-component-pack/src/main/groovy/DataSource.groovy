/**
 * Attaches values from a CSV-file to incoming Virtual Users. These values can
 * then be used by subsequent components, such as a SoapUI Runner.
 *
 * @id com.eviware.DataSource
 * @category flow
 * @dependency com.xlson.groovycsv:groovycsv:1.0
 */
 
import com.xlson.groovycsv.CsvParser
import java.util.ArrayList
import com.google.common.collect.Lists



createOutgoing( 'output' ) // Creates an outgoing connector called "output".
csvIterator = null

def inputFileText;

def dataList;

createProperty( 'separatorSymbol', String, ',' )

parseCsv = {
	if( inputFile.value )
	{
		dataList = Lists.newArrayList(CsvParser.parseCsv( inputFile.value.text, separator: separatorSymbol.value) as Iterator)
		resetIterator()
	}
}

resetIterator = {
	csvIterator = dataList.iterator()
}

createProperty( 'inputFile', File ) {
	parseCsv() // This will be called whenever the property's value is changed.
}

createProperty( 'shouldLoop', Boolean, false ) 

// This is called whenever we get an incoming message.
onMessage = { sendingConnector, receivingConnector, message ->

	if(!csvIterator?.hasNext() && shouldLoop.value)
	{
		resetIterator()
	}
		
	if( csvIterator?.hasNext() )
	{
		line = csvIterator.next()
		line.columns.each{ key, index ->
			message[key] = line.values[index]
		}
	}

	send( output, message )
}

onAction( 'RESET' ) {
	parseCsv()
}

layout { 
	property( property:inputFile, constraints: 'width 220', label:'Input file' )
	property( property:shouldLoop, label:'Loop' )
}

settings (label: 'General') {
	property( property: separatorSymbol, label:'Separator' )
}

parseCsv()
