package hu.blackbelt.judo.tatami.jsl.workflow.maven.plugin;

/*-
 * #%L
 * JUDO Tatami JSL parent
 * %%
 * Copyright (C) 2018 - 2022 BlackBelt Technology
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */

import lombok.Builder;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

@Builder
public class ArtifactResolver {
    final int BUFFER_SIZE = 4096;

    @NonNull
    private MavenProject project;

    @NonNull
    private RepositorySystem repoSystem;

    @NonNull
    private RepositorySystemSession repoSession;


    @NonNull
    private List<RemoteRepository> repositories;

    @NonNull
    private Log log;

    @SneakyThrows({MalformedURLException.class})
    private URL getArtifactFile(Artifact artifact) {
        return artifact.getFile().toURI().toURL();
    }


    /**
     * Get the artifact file from the given url.
     * @param url
     * @return
     * @throws MojoExecutionException
     */
    @SuppressWarnings("null")
    public File getArtifact(String url) throws MojoExecutionException {
        if (url.startsWith("mvn:")) {
            String mvnUrl = url;
            String subUrl = "";
            if (mvnUrl.contains("!")) {
                subUrl = mvnUrl.substring(mvnUrl.lastIndexOf("!") + 1);
                mvnUrl = mvnUrl.substring(0, mvnUrl.lastIndexOf("!"));
            }
            ArtifactResult resolutionResult = getArtifactResult(mvnUrl);
            // The file should exists, but we never know.
            File file = resolutionResult.getArtifact().getFile();
            if (file == null || !file.exists()) {
                log.warn("Artifact " + url.toString() + " has no attached file. Its content will not be copied in the target model directory.");
            }

            if (subUrl.equals("")) {
                return file;
            } else {
                // Extract file from JAR or ZIP
                File fileFromArchive = null;
                try {
                    fileFromArchive = getFileFromArchive(file, subUrl);
                } catch (IOException e) {
                    throw new MojoExecutionException("Could not decompress: " + fileFromArchive.getAbsolutePath() + " file: " + fileFromArchive);
                }
                if (fileFromArchive == null || !fileFromArchive.exists()) {
                    throw new MojoExecutionException("File " + subUrl + " does not exists in " + file.getAbsolutePath());
                }
                return fileFromArchive;
            }
        } else {
            File file = new File(url);
            if (file == null || !file.exists()) {
                log.warn("File " + url.toString() + " does not exists.");
            }
            return file;
        }
    }

    public File getFileFromArchive(File archive, String path) throws IOException {
        CompressorInputStream compressorInputStream = null;
        ArchiveInputStream archiveInputStream = null;
        File outFile = null;
        try {
            if (archive.getName().toLowerCase().endsWith(".tgz") || archive.getName().toLowerCase().endsWith(".tar.gz")) {
                compressorInputStream = new GzipCompressorInputStream(new FileInputStream(archive));
                archiveInputStream = new TarArchiveInputStream(compressorInputStream);
            } else if (archive.getName().toLowerCase().endsWith(".zip") || archive.getName().toLowerCase().endsWith(".jar")) {
                archiveInputStream = new ZipArchiveInputStream(new FileInputStream(archive));
            } else if (archive.getName().toLowerCase().endsWith(".bz2") || archive.getName().toLowerCase().endsWith(".tar.bzip2")) {
                compressorInputStream = new BZip2CompressorInputStream(new FileInputStream(archive));
                archiveInputStream = new TarArchiveInputStream(compressorInputStream);
            }

            if (archiveInputStream == null) {
                throw new IOException("Could not open: " + archive.getAbsolutePath());
            }
            if (archiveInputStream != null) {
                ArchiveEntry entry;
                while ((entry = archiveInputStream.getNextEntry()) != null) {
                    if (!entry.isDirectory() && entry.getName().equals(path)) {
                        FileNameUtils.getExtension(path);
                        outFile = File.createTempFile("artifacthandler", entry.getName().replaceAll("/", "_"));
                        int count;
                        byte data[] = new byte[BUFFER_SIZE];
                        FileOutputStream fos = new FileOutputStream(outFile, false);
                        try (BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER_SIZE)) {
                            while ((count = archiveInputStream.read(data, 0, BUFFER_SIZE)) != -1) {
                                dest.write(data, 0, count);
                            }
                        }
                        return outFile;
                    }
                }
            }
        } finally {
            if (compressorInputStream != null) {
                try {
                    compressorInputStream.close();
                } catch (Exception e) {
                }
            }

            if (archiveInputStream != null) {
                try {
                    archiveInputStream.close();
                } catch (Exception e) {
                }
            }
        }
        return null;
    }

    /**
     * Get the artifact result from the given url.
     * @param url
     * @return
     * @throws MojoExecutionException
     */
    public ArtifactResult getArtifactResult(String url) throws MojoExecutionException {
        org.eclipse.aether.artifact.Artifact artifact = new DefaultArtifact(url.toString().substring(4));
        ArtifactRequest req = new ArtifactRequest().setRepositories(this.repositories).setArtifact(artifact);
        ArtifactResult resolutionResult;
        try {
            resolutionResult = repoSystem.resolveArtifact(repoSession, req);

        } catch (ArtifactResolutionException e) {
            throw new MojoExecutionException("Artifact " + url.toString() + " could not be resolved.", e);
        }
        return resolutionResult;
    }

}
