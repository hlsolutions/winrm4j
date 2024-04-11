package io.cloudsoft.winrm4j.client.psrp;

import java.util.UUID;

import org.testng.Assert;
import org.testng.annotations.Test;

public class PsrpPipelineResponseMessageParserTest {

	@Test
	public void testDecodePipelineOutput_SpecialChars() {
		final var actual = PsrpPipelineResponseMessageParser.INSTANCE.decodePipelineOutput(new PsrpMessage(
				UUID.randomUUID(),
				UUID.randomUUID(),
				PsrpMessageType.PIPELINE_OUTPUT,
				PsrpDestination.CLIENT,
				"""
						<S>[_x000D__x000A_    {_x000D__x000A_        "LogicalProcessor":  0,_x000D__x000A_        "NumaNode":  0,_x000D__x000A_        "Socket":  0,_x000D__x000A_        "Core":  0,_x000D__x000A_        "Thread":  0_x000D__x000A_    },_x000D__x000A_    {_x000D__x000A_        "LogicalProcessor":  1,_x000D__x000A_        "NumaNode":  0,_x000D__x000A_        "Socket":  0,_x000D__x000A_        "Core":  0,_x000D__x000A_        "Thread":  1_x000D__x000A_    },_x000D__x000A_    {_x000D__x000A_        "LogicalProcessor":  2,_x000D__x000A_        "NumaNode":  0,_x000D__x000A_        "Socket":  0,_x000D__x000A_        "Core":  1,_x000D__x000A_        "Thread":  0_x000D__x000A_    },_x000D__x000A_    {_x000D__x000A_        "LogicalProcessor":  3,_x000D__x000A_        "NumaNode":  0,_x000D__x000A_        "Socket":  0,_x000D__x000A_        "Core":  1,_x000D__x000A_        "Thread":  1_x000D__x000A_    },_x000D__x000A_    {_x000D__x000A_        "LogicalProcessor":  4,_x000D__x000A_        "NumaNode":  0,_x000D__x000A_        "Socket":  0,_x000D__x000A_        "Core":  2,_x000D__x000A_        "Thread":  0_x000D__x000A_    },_x000D__x000A_    {_x000D__x000A_        "LogicalProcessor":  5,_x000D__x000A_        "NumaNode":  0,_x000D__x000A_        "Socket":  0,_x000D__x000A_        "Core":  2,_x000D__x000A_        "Thread":  1_x000D__x000A_    },_x000D__x000A_    {_x000D__x000A_        "LogicalProcessor":  6,_x000D__x000A_        "NumaNode":  0,_x000D__x000A_        "Socket":  0,_x000D__x000A_        "Core":  3,_x000D__x000A_        "Thread":  0_x000D__x000A_    },_x000D__x000A_    {_x000D__x000A_        "LogicalProcessor":  7,_x000D__x000A_        "NumaNode":  0,_x000D__x000A_        "Socket":  0,_x000D__x000A_        "Core":  3,_x000D__x000A_        "Thread":  1_x000D__x000A_    }_x000D__x000A_]</S>
						"""
		));
		final var expected = "[\r\n" +
				"    {\r\n" +
				"        \"LogicalProcessor\":  0,\r\n" +
				"        \"NumaNode\":  0,\r\n" +
				"        \"Socket\":  0,\r\n" +
				"        \"Core\":  0,\r\n" +
				"        \"Thread\":  0\r\n" +
				"    },\r\n" +
				"    {\r\n" +
				"        \"LogicalProcessor\":  1,\r\n" +
				"        \"NumaNode\":  0,\r\n" +
				"        \"Socket\":  0,\r\n" +
				"        \"Core\":  0,\r\n" +
				"        \"Thread\":  1\r\n" +
				"    },\r\n" +
				"    {\r\n" +
				"        \"LogicalProcessor\":  2,\r\n" +
				"        \"NumaNode\":  0,\r\n" +
				"        \"Socket\":  0,\r\n" +
				"        \"Core\":  1,\r\n" +
				"        \"Thread\":  0\r\n" +
				"    },\r\n" +
				"    {\r\n" +
				"        \"LogicalProcessor\":  3,\r\n" +
				"        \"NumaNode\":  0,\r\n" +
				"        \"Socket\":  0,\r\n" +
				"        \"Core\":  1,\r\n" +
				"        \"Thread\":  1\r\n" +
				"    },\r\n" +
				"    {\r\n" +
				"        \"LogicalProcessor\":  4,\r\n" +
				"        \"NumaNode\":  0,\r\n" +
				"        \"Socket\":  0,\r\n" +
				"        \"Core\":  2,\r\n" +
				"        \"Thread\":  0\r\n" +
				"    },\r\n" +
				"    {\r\n" +
				"        \"LogicalProcessor\":  5,\r\n" +
				"        \"NumaNode\":  0,\r\n" +
				"        \"Socket\":  0,\r\n" +
				"        \"Core\":  2,\r\n" +
				"        \"Thread\":  1\r\n" +
				"    },\r\n" +
				"    {\r\n" +
				"        \"LogicalProcessor\":  6,\r\n" +
				"        \"NumaNode\":  0,\r\n" +
				"        \"Socket\":  0,\r\n" +
				"        \"Core\":  3,\r\n" +
				"        \"Thread\":  0\r\n" +
				"    },\r\n" +
				"    {\r\n" +
				"        \"LogicalProcessor\":  7,\r\n" +
				"        \"NumaNode\":  0,\r\n" +
				"        \"Socket\":  0,\r\n" +
				"        \"Core\":  3,\r\n" +
				"        \"Thread\":  1\r\n" +
				"    }\r\n" +
				"]\r\n";
		Assert.assertEquals(actual, expected, "invalid pipeline output");
	}

}