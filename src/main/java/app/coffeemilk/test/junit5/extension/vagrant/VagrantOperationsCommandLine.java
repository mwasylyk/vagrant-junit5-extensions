/*
 * vagrant-junit5-extensions
 * Copyright (C) 2018 - coffeemilk.app
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package app.coffeemilk.test.junit5.extension.vagrant;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

// TODO Very earl on WIP
@Slf4j
public class VagrantOperationsCommandLine implements VagrantOperations {

	// TODO convert to enum
	public static final String[] statusHeaders = new String[]{"timestamp", "target", "type", "data"};
	private Path vagrantfile;

	public VagrantOperationsCommandLine() {
		setVagrantfile(Paths.get(".").toAbsolutePath());
	}

	public VagrantOperationsCommandLine(Path vagrantfile) {
		setVagrantfile(vagrantfile);
	}

	@Override
	public boolean isUp() {
		log.info("Checking if Vagrant VM is up");

		boolean up = false;
		try {
			String result = new ProcessExecutor().command("vagrant", "status", "--machine-readable")
					.readOutput(true).execute()
					.outputUTF8();
			log.info("Status:[{}]", result);
			CSVParser parser = CSVParser.parse(result, CSVFormat.DEFAULT.withHeader(statusHeaders));

			CSVRecord found = parser.getRecords().stream()
					.filter(record -> record.get(statusHeaders[2]).equals("state"))
					.findFirst().orElseThrow(IllegalArgumentException::new);
			up = found.get("data").equals("running");

			log.info("Vagrant Up:[{}]", up);
			return up;
		} catch (IOException | TimeoutException | InterruptedException e) {
			e.printStackTrace();
		}

		return up;
	}

	@Override
	public boolean isDown() {
		return !isUp();
	}

	@Override
	public void startup() throws Exception {
		log.info("Starting Vagrant");
		ProcessResult upResult = new ProcessExecutor()
				.command("vagrant", "up")
				.readOutput(true)
				.redirectOutput(Slf4jStream.of("VagrantStartup").asInfo())
				.execute();
		log.info("Vagrant Up\n{}", upResult.getOutput().getUTF8());
//		int exitCode = new ProcessExecutor()
////				.command("vagrant", "up")
////				.execute().getExitValue();
		log.info("Executed 'vagrant up', exitCode:[{}]", upResult.getExitValue());
		if (upResult.getExitValue() != 0) {
			throw new RuntimeException();
		}
	}

	@Override
//	@Retryable(value = VagrantOperationException.class, maxAttempts = 100, backoff = @Backoff(delay = 500))
	public void waitForStartup() throws Exception {
		log.info("Waiting for Vagrant VM to start");
		if (isDown()) {
			startup();
//			throw new VagrantOperationException();
		}

		while (isDown()) {
			log.info("Checking Vagrant");
			Thread.sleep(TimeUnit.SECONDS.toMillis(10));
			if (isUp()) {
				log.info("Vagrant Started!!!");
				break;
			}
		}
	}

	@Override
	public void shutdown() {
		log.info("no-op shutdown");
	}

	@Override
	public void reload(boolean provision) throws InterruptedException, TimeoutException, IOException {
		log.info("Reloading Vagrant VM");

		List<String> command = new ArrayList<>();
		command.add("vagrant");
		command.add("reload");
		if (provision) {
			command.add("--provision");
		}

		log.info("Reload:[{}]", command);
		ProcessResult reload = new ProcessExecutor()
				.command(command)
				.readOutput(true)
				.redirectOutput(Slf4jStream.of("VagrantStartup").asInfo())
				.execute();
		log.info("Vagrant Up\n{}", reload.getOutput().getUTF8());
	}

	@Override
	public Path getVagrantfile() {
		return this.vagrantfile;
	}

	@Override
	public void setVagrantfile(Path vagrantfilePath) {
		this.vagrantfile = vagrantfilePath;
	}

}
