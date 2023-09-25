import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
 
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ocsp.BasicOCSPResponse;
import org.bouncycastle.asn1.ocsp.OCSPResponse;
import org.bouncycastle.asn1.util.ASN1Dump;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
 
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
 
import gov.treasury.pki.json.vss.OCSPResponseList;
import gov.treasury.pki.json.vss.VSSRequest;
import gov.treasury.pki.json.vss.VSSResponse;
import gov.treasury.pki.json.vss.WantBackTypeToken;
import gov.treasury.pki.json.vss.X509CertificateList;
import gov.treasury.pki.rest.RestClient;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
 
public class VSSCerts {
 
                static boolean debug = false;
 
                public static void main(String[] args) throws IOException {
                                ArgumentParser parser = ArgumentParsers
                                                                .newFor("java -cp VACStoPEM.jar gov.treasury.pki.VSSCerts").build()
                                                                .defaultHelp(true).description("Validate a file of PEM certificates using Treasury VSS Rest API");
                                parser.addArgument("-s", "--service")
                                                                .choices("vssapi-acc.treasury.gov:443", "vssapi-dev.treasury.gov:443", "vssapi.treasury.gov:443")
                                                                .setDefault("vssapi-dev.treasury.gov:443").help("Specify rest service to use").required(false);
                                parser.addArgument("-p", "--policy").setDefault("1.3.6.1.5.5.7.19.1").help("Specify validation policy to use")
                                                                .required(false);
                                parser.addArgument("-i", "--infile").help("Input file of PEM encoded certificates").required(true);
                                parser.addArgument("-o", "--outfile").help("Output file of PEM encoded certificates").required(false);
                                parser.addArgument("-v", "--verbose").help("Detailed output").action(Arguments.storeConst()).setConst(true)
                                                                .setDefault(false).required(false);
                                Namespace ns = null;
                                try {
                                                ns = parser.parseArgs(args);
                                } catch (ArgumentParserException e) {
                                                parser.handleError(e);
                                                System.exit(1);
                                }
                                debug = ns.getBoolean("verbose");
                                String service = ns.getString("service");
                                String policy = ns.getString("policy");
                                if (null == ns.getString("infile")) {
                                                System.err.println("Input file must be specified!");
                                                parser.printHelp();
                                                System.exit(1);
                                }
                                File infile = new File(ns.getString("infile"));
                                File outfile = null;
                                FileWriter validWriter = null;
                                if (null != ns.getString("outfile")) {
                                                outfile = new File(ns.getString("outfile"));
                                                validWriter = new FileWriter(outfile);
                                }
                                System.out.println("Processing File:  " + infile.getAbsolutePath());
                                try (BufferedReader br = new BufferedReader(new FileReader(infile))) {
                                                for (String line; (line = br.readLine()) != null;) {
                                                                byte[] certData = null;
                                                                if (line == "" || line.isEmpty()) {
                                                                                /*
                                                                                * Swallow blank line
                                                                                */
                                                                } else if (line.startsWith("subject=") || line.startsWith("issuer=") || line.startsWith("Subject=")
                                                                                                || line.startsWith("Issuer=") || line.startsWith("NotBefore=")
                                                                                                || line.startsWith("NotAfter=")) {
                                                                                /*
                                                                                * Swallow PEM comment
                                                                                */
                                                                                certData = null;
                                                                } else if (line.startsWith("-----BEGIN CERTIFICATE-----")) {
                                                                                StringBuffer sb = new StringBuffer();
                                                                                sb.append(line + "\n");
                                                                                while (!line.startsWith("-----END CERTIFICATE-----")) {
                                                                                                line = br.readLine();
                                                                                                sb.append(line + "\n");
                                                                                }
                                                                                sb.append("-----END CERTIFICATE-----" + "\n");
                                                                                certData = sb.toString().getBytes();
                                                                } else {
                                                                                System.err
                                                                                                                .println("Unrecognized file format!" + "\nThis code only supports the following encodings, "
                                                                                                                                                + "where each certificate is the only data on each line:"
                                                                                                                                                + "\n\t-base64 (with -or- without PEM tags)");
                                                                                System.exit(0);
                                                                }
                                                                if (null != certData) {
                                                                                CertificateFactory cf = null;
                                                                                X509Certificate cert = null;
                                                                                cf = CertificateFactory.getInstance("X.509");
                                                                                try {
                                                                                                cert = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certData));
                                                                                } catch (CertificateParsingException e) {
                                                                                                System.err.println("Failed to parse certificate: " + e.getLocalizedMessage());
                                                                                                System.err.println(toPEM(certData));
                                                                                                continue;
                                                                                } catch (CertificateException e) {
                                                                                                System.err.println("Failed to parse certificate: " + e.getLocalizedMessage());
                                                                                                System.err.println(toPEM(certData));
                                                                                                continue;
                                                                                }catch (Exception e) {
                                                                                                System.err.println("Failed to parse certificate: " + e.getLocalizedMessage());
                                                                                                System.err.println(toPEM(certData));
                                                                                                continue;
                                                                                }
                                                                                if (debug) {
                                                                                                System.out.println("Validating Certificate: " + cert.getSubjectX500Principal().getName());
                                                                                                System.out.println(toPEM(cert));
                                                                                }
                                                                                List<WantBackTypeToken> wantBackList = new ArrayList<WantBackTypeToken>();
                                                                                WantBackTypeToken certPath = new WantBackTypeToken();
                                                                                certPath.wantBackTypeToken = "certPath";
                                                                                wantBackList.add(certPath);
                                                                                WantBackTypeToken revocationInfo = new WantBackTypeToken();
                                                                                revocationInfo.wantBackTypeToken = "revocationInfo";
                                                                                wantBackList.add(revocationInfo);
                                                                                VSSResponse response = validateCert(service, cert, policy, wantBackList);
                                                                                if (!debug) {
                                                                                                System.out.print(".");
                                                                                }
                                                                                ObjectMapper objectMapper = new ObjectMapper();
                                                                                if (debug) {
                                                                                                System.out.println(objectMapper.writeValueAsString(response));
                                                                                                System.out.println("Issuer: "
                                                                                                                                + response.validationResult.resultsByCertificateList
                                                                                                                                                                .get(0).resultByCertificate.x509IssuerName
                                                                                                                                + "\n" + "Subject: "
                                                                                                                                + response.validationResult.resultsByCertificateList
                                                                                                                                                                .get(0).resultByCertificate.x509SubjectName
                                                                                                                                + "\n" + "Validation Result: " + response.validationResult.resultsByCertificateList
                                                                                                                                                                .get(0).resultByCertificate.validationResultToken);
                                                                                }
                                                                                if (response.validationResult.resultsByCertificateList
                                                                                                                .get(0).resultByCertificate.validationResultToken.equalsIgnoreCase("FAIL")) {
                                                                                                if (debug) {
                                                                                                                System.out.println("Will not include due to validation failure: "
                                                                                                                                                + response.validationResult.resultsByCertificateList
                                                                                                                                                                                .get(0).resultByCertificate.validationFailureData.invalidityReasonList
                                                                                                                                                                                                                .get(0).invalidityReasonText);
                                                                                                }
                                                                                } else {
                                                                                                if (null != outfile) {
                                                                                                                validWriter.write(toPEM(cert));
                                                                                                }
                                                                                                /*
                                                                                                * Temp code to eval RFC822Mail values
                                                                                                */
                                                                                                /*
                                                                                                 * TempMailParseForPF.processResult(response.validationResult.resultsByCertificateList
                                                                                                *                            .get(0).resultByCertificate);
                                                                                                */
                                                                                                /*
                                                                                                * End Temp code
                                                                                                */
                                                                                                X509CertificateList bestPath = response.validationResult.resultsByCertificateList
                                                                                                                                .get(0).resultByCertificate.validationSuccessData.wantBackResultList.get(0).certPath;
                                                                                                for (gov.treasury.pki.json.vss.X509Certificate currentCert : bestPath.x509CertificateList) {
                                                                                                                X509Certificate pathCert = (X509Certificate) cf.generateCertificate(
                                                                                                                                                new ByteArrayInputStream(Base64.decodeBase64(currentCert.x509Certificate)));
                                                                                                                if (debug) {
                                                                                                                                System.out.println(pathCert);
                                                                                                                                System.out.println(toPEM(pathCert));
                                                                                                                }
                                                                                                }
                                                                                                OCSPResponseList revocationData = response.validationResult.resultsByCertificateList
                                                                                                                                .get(0).resultByCertificate.validationSuccessData.wantBackResultList
                                                                                                                                                                .get(1).revocationInfo;
                                                                                                for (gov.treasury.pki.json.vss.OCSPResponse currentResponse : revocationData.ocspResponseList) {
                                                                                                                OCSPResponse ocspResponse = OCSPResponse.getInstance(
                                                                                                                                                ASN1Primitive.fromByteArray(Base64.decodeBase64(currentResponse.ocspResponse)));
                                                                                                                byte[] respBytes = ocspResponse.getResponseBytes().getResponse().getOctets();
                                                                                                                BasicOCSPResponse bResp = BasicOCSPResponse
                                                                                                                                                .getInstance(ASN1Primitive.fromByteArray(respBytes));
                                                                                                                if (debug) {
                                                                                                                                System.out.println(
                                                                                                                                                                "OCSP Response: " + ASN1Dump.dumpAsString(bResp.getTbsResponseData(), true));
                                                                                                                }
                                                                                                }
                                                                                }
                                                                }
                                                }
                                } catch (CertificateException | FileNotFoundException e) {
                                                e.printStackTrace();
                                }
                                if (null != outfile) {
                                                validWriter.flush();
                                                validWriter.close();
                                }
                }
 
                /**
                * POST /vss/rest/
                *
                 * @param cert
                * @param valPol
                * @param wantBackList
                */
                public static VSSResponse validateCert(String vssHost, X509Certificate cert, String valPol,
                                                List<WantBackTypeToken> wantBackList) {
                                byte[] certBytes = null;
                                try {
                                                certBytes = cert.getEncoded();
                                } catch (CertificateEncodingException e) {
                                                e.printStackTrace();
                                }
                                RestClient client = RestClient.instance();
                                gov.treasury.pki.json.vss.X509Certificate certList[] = new gov.treasury.pki.json.vss.X509Certificate[1];
                                gov.treasury.pki.json.vss.X509Certificate jsonCert = new gov.treasury.pki.json.vss.X509Certificate();
                                jsonCert.x509Certificate = Base64.encodeBase64String(certBytes);
                                jsonCert.vssCertId = getVssCertId(cert);
                                certList[0] = jsonCert;
                                VSSRequest request = new VSSRequest(valPol, wantBackList, Arrays.asList(certList));
                                ObjectMapper objectMapper = new ObjectMapper();
                                try {
                                                if (debug) {
                                                                System.out.println(objectMapper.writeValueAsString(request));
                                                }
                                } catch (JsonProcessingException e) {
                                                e.printStackTrace();
                                }
                                return client.vssRequest(https:// + vssHost + "/vss/rest/", request);
                }
 
                /**
                * Return Hex String of SHA-256 digest of the certificate
                *
                 * @param cert
                * @return String vss_cert_id
                */
                public static String getVssCertId(X509Certificate cert) {
                                byte[] digest = null;
                                MessageDigest md = null;
                                try {
                                                md = MessageDigest.getInstance("SHA-256");
                                                md.update(cert.getEncoded());
                                                digest = md.digest();
                                } catch (NoSuchAlgorithmException | CertificateEncodingException e) {
                                                e.printStackTrace();
                                }
 
                                return byteArrayToString(digest);
                }
 
                /**
                * Convert a byte array to a Hex String
                *
                 * The following method converts a byte[] object to a String object, where the
                * only output characters are "0123456789ABCDEF".
                *
                 * @param ba A byte array
                *
                 * @return String Hexidecimal String object which represents the contents of the
                *         byte array
                */
                public static String byteArrayToString(byte[] ba) {
                                if (ba == null) {
                                                return "";
                                }
                                StringBuffer hex = new StringBuffer(ba.length * 2);
                                for (int i = 0; i < ba.length; i++) {
                                                hex.append(Integer.toString((ba[i] & 0xff) + 0x100, 16).substring(1));
                                }
                                return hex.toString().toUpperCase(Locale.US);
                }
 
                private static final String numbers = "0123456789ABCDEF";
 
                /**
                * Convert a Hex String to a byte array
                *
                 * The following method converts a String object to a byte[] object, where the
                * only valid input characters is "0123456789ABCDEF".
                *
                 * @param s Hexidecimal String object to convert to a byte array
                *
                 * @return byte[] A byte array
                */
                public static byte[] stringToByteArray(String s) {
                                if (s == null)
                                                return null;
                                byte[] result = new byte[s.length() / 2];
                                for (int i = 0; i < s.length(); i += 2) {
                                                int i1 = numbers.indexOf(s.charAt(i));
                                                int i2 = numbers.indexOf(s.charAt(i + 1));
                                                result[i / 2] = (byte) ((i1 << 4) | i2);
                                }
                                return result;
                }
 
                /**
                *
                 * @param cert
                * @return String
                * @throws CertificateException
                * @throws IOException
                */
                public static String toPEM(X509Certificate cert) throws CertificateException {
                                StringBuffer sb = new StringBuffer();
                                sb.append("Subject=" + cert.getSubjectX500Principal().getName() + "\n");
                                sb.append("Issuer=" + cert.getIssuerX500Principal().getName() + "\n");
                                sb.append("NotBefore=" + cert.getNotBefore() + "\n");
                                sb.append("NotAfter=" + cert.getNotAfter() + "\n");
                                sb.append(toPEM(cert.getEncoded()));
                                return sb.toString();
                }
 
                public static String toPEM(byte[] certData) {
                                StringBuffer sb = new StringBuffer();
                                PemObject certPem = new PemObject("CERTIFICATE", certData);
                                StringWriter sw = new StringWriter();
                                PemWriter writer = new PemWriter(sw);
                                try {
                                                writer.writeObject(certPem);
                                                writer.flush();
                                                writer.close();
                                } catch (IOException e) {
                                                e.printStackTrace();
                                }
                                sb.append(sw.getBuffer());
                                return sb.toString();
                }
}