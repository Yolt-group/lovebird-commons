package nl.ing.lovebird.secretspipeline;

import nl.ing.lovebird.secretspipeline.converters.TestUtil;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.bouncycastle.openpgp.PGPException;
import org.c02e.jpgpj.CompressionAlgorithm;
import org.c02e.jpgpj.Decryptor;
import org.c02e.jpgpj.EncryptionAlgorithm;
import org.c02e.jpgpj.Encryptor;
import org.c02e.jpgpj.HashingAlgorithm;
import org.c02e.jpgpj.Key;
import org.c02e.jpgpj.Ring;
import org.jose4j.base64url.internal.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static nl.ing.lovebird.secretspipeline.converters.KeyStoreReader.TYPE_INDICATOR;
import static org.junit.jupiter.api.Assertions.assertEquals;

class KeyRingCreatorTest {

    @Test
    void testKeyRingCreationNoKeys() {
        Assertions.assertThatExceptionOfType(SecurityException.class)
                .isThrownBy(() -> new KeyRingCreator(Paths.get("nothere").toUri()).createKeyRing());
    }

    @Test
    void testKeyRingCreationWithKeys(@TempDir Path tempDir) throws IOException {
        String pgpkey = "-----BEGIN PGP PRIVATE KEY BLOCK-----\n" +
                "\n" +
                "lQPGBFyKMyEBCACwIgX6oYBxHasN6irUhxY0QdO+veUQmElH07WqwLhCRNo6mLBX\n" +
                "Oi/OBIBBUN/w36LyR0YVovk2fCfA2WaeHHkLeDKo1HoHIz7ByrV03Jlp7aQkV3G2\n" +
                "z9D2fEGmFnbJ2mclh7bDKsQPJkjL2dsuYrlCzNUCw3m1Jf2HhW1q6JkU2JpHfmW+\n" +
                "ho+9ZIBWn1F4yJXYot5ziSvDjiJjcLTK26unNfB7C50wdwpSz5IlV6h7pW4XL44d\n" +
                "a/q5QeUp8Swgu736m0ahgwkX2DpxwoZSBGGVBYaElE1W157H/Q8sDi3BBvEe49mZ\n" +
                "j4+F1mNw3ijmdAAHreu7jdC2SVlDbiSsR6stABEBAAH+BwMCloTWoxEIK2Xpwz+v\n" +
                "ztCf7YPmIj0S0bcEvox24a+MdUj41DdNoqNMvFFybEedeOgTjzHNiDTPVWDTTAjh\n" +
                "131iilL54oe+YsO7DhcXiTGT2GhPZoGonl4b7Vmotx50WsnO6GfNivZoLubsUhDA\n" +
                "45SfoVpWrbJdakW/Cqz8d2LtxtXfKt8LPk84pfE5S2PHAyVF+1mOciXHfhxQ6Cpj\n" +
                "9yuQ5Ymf249TaCqBMgy9oZkFCGn49gowDxc4F6mtPNy+YZ6LtLUauYZkSnjPu2C6\n" +
                "lor9tbSzIhjMWux67UjrDvdzkbzqS/PRUQHjP/CXD9l13ZJ2Fzbu7xH2XsN5AXpP\n" +
                "ctWF32b+E7KPJGXztZSjsb8FL7eGQhK8s9CgULhYbN095Xo9Aa9eYISZhyJDV4z5\n" +
                "3QZMhJHwUT+ReH1PW/tizklAYt5hDUksX7aDH5gk5us1uQOdb01abEAOkNZYvb+l\n" +
                "KnqqXK51y+bP/H9mumyN4jV82QRQKSq6Nt5e8g1qQyisvxPTBK9Bbr0XFTfT7Zxq\n" +
                "KOK3zaqh/PQMRs9tkoi+/gTWAaCAxAuGGEleZTNfEmSjejAezxHnTwVPFiAEUD5M\n" +
                "5p+TtuLh9QYBYnV/qYmGXMt+b++v1NCFxJaaiqDARw3yBg7axsinD+RnQmE4xFMK\n" +
                "X5xeKbKbVUThsjMJvhHDYdFLpd6W4rz3SxzUCC4F0uczqKuksgXh+oQWwy2PA8LE\n" +
                "hOo6ELYjUm43MDmJdqlQQlUvFueszhJ3q9iNSmuLoBXmjPRaXxFY531nYNM8Dorz\n" +
                "E6cS3E1HUFBUNkhqNRQRxYGiLGOjq8RV4dzsg4ybdRTrF+XDCc9fZozd5b3bauye\n" +
                "a7UXc70nQPoxHSR2K2UaZSx2DrO2B7R81T8Cqf7D9yOXFB2Mh4mAx94GKvddfCe0\n" +
                "FIs2y10whwiDtBt5b2x0LWt5Yy1kZXYgPGRldkB5b2x0LmNvbT6JAU4EEwEIADgW\n" +
                "IQRRdsM6LZM762bbsEL19wy1pogHpwUCXIozIQIbDwULCQgHAgYVCgkICwIEFgID\n" +
                "AQIeAQIXgAAKCRD19wy1pogHpyWqCACERD3PbEMVDHSCaIymGqiQ3/nI5i/GvQL2\n" +
                "PMwCxs0DEyFni0++QMKwnrhdC9BZh27JbExQmcgIOLS0THfIjXZlUb4uE9dnseXJ\n" +
                "6/A01BhOhbRSXkjTv9Iv7pGn0Zsdl7EQ3NJsYl6lqRa3bWAfMfx4zDvqIeHDYOqT\n" +
                "DOtO2C8RRUh8u9lSBnSMgAcNiO2BqmWrR4sdyU5vGfV8eltl8KwVhlo8NwUUZAzm\n" +
                "ZSvQMGD4GbUJf3i6TmX82z2mWAJtAWeDrKkfSfj7bX67j7asmId8VSauYniSd7QG\n" +
                "xEqOIL6a0WK3DRkWNb42qIDc0ws0VhNbq8OVOTVYIG3XripIUnun\n" +
                "=wlog\n" +
                "-----END PGP PRIVATE KEY BLOCK-----";
        Path createdFile = tempDir.resolve("symmetric_GPG");
        Files.write(createdFile, Lists.newArrayList(TYPE_INDICATOR + "GPG"));
        Files.write(createdFile, Base64.encodeBase64(pgpkey.getBytes(UTF_8)), StandardOpenOption.APPEND);

        Ring keyRing = new KeyRingCreator(createdFile.toUri()).createKeyRing().getRing();

        assertEquals(1, keyRing.getKeys().size());
    }

    @Test
    void testWithExistingPGPKeyPair(@TempDir Path tempDir) throws Exception {
        String keypair = "LS0tLS1CRUdJTiBQR1AgUFJJVkFURSBLRVkgQkxPQ0stLS0tLQpWZXJzaW9uOiBCQ1BHIHYxLjYzCgpsUWRHQkFBYzVGQURFQURMZGh3bzRJQWkxdUVNQUc0L3dKc0hiekFRdVlOVnhQQXB1QzlnSTZ1djZHdnc1b3NTCkt1RmcweVJWNWF6alpXLzZxMU1RYXg0T0h6MzBhbzI2eGRiaTIzWjhXbE1kK1g1d25TSUZpNFZ4NHczTll0cFcKVXZwdGdvMTVBdi8xZllaYkRXR1pqTEg5dm9QbWNxTCs0c3piZnRqczZBcGI4RG82ajlXVVJ5WHdYd09JM2hwWgpQVTlPWnBhcXlxWmlkS2Z0Rzdib2FBZVNySzg4d0gzalh1Q21UNWthUHBYYXMwaFBWVVpUR1BMY1pSYjBReVhmCm5yaWN0Skp4U2dEb0xNMlJWbXdBdFhabXNjS2hFbjQ1MkwrdjREVm5HczJscFhwaXJ5RmpUc0lFNFJRbUhkRDEKbjJpZFc3a2tqWXh6NzRaVXVsOGZRNkJycFN0TWtKWGlVaksrRFlrWjlKam93M29HQkFob0QxRHl1TEUzSUUwQQpEa08zdXlBZDA3d2FIVGZqLzZqWnRCblNPZWR3OGlsNDBIcStTemdpYkhMVGxLcjlEVG9yNE5kcHFPbUNBRjlqCnhDaFkveEJieVJNWlhZL0hyZ2hIdXNYR05MTi9vb21KU2pZdFI1LzYwN3Fha095SFh5Z0tuVktrRE9JRzdkck0KR0FyaUNEQ1RFc0FCc3E1QkVNdDR0cUduZU5QcHZpOEtiVnpYQVp4dEVFNkNjOHVJYkt2Uzl2dm9uUDhvem1Bdgp0Q0lyWmFoWEE0c1ZNTEQwejhvTThxUW5QdjBKWFJkRjJrbEpFZUFGNFdUbXlsZU1VM1kyMlJUcjdWcFlUUHBIClk1M2FmZlNSR0s3Q002S254OGgzbHdibkFhclFwOGFTcGtSdnE2N1ZRd3pla2lwWmZJTW1IU1hscHdBUkFRQUIKL2drRENJVERzWGRwME8ybHdEUHozRnQ4VzN6SXVvdGVQK01Sc1ZQTmtPMDZGN3FydWJQOU5FS1pzOXlxbkxhVQplMWM3ZmFYMVlnbGdzUEtvUGx4YXJ5YkE5VGJKc0xSTk1URnhZZWpkdFRJU1o4aWdiQkg1UVlmYTBYTks2Zm0xCnJCUGNqemFIenpBT1FNMGc2OVlqaGxaWFliU3piZEdzUyt3R25nT05WaDBXSlpvQko4TnN0ZkRmRVcyb0VYRVMKZURFRzdCK1NVTDY5bFRlUVVHaU1ZT053VmJWc0J4QS83YlVlc285YkpaVTNpVUhpSHlRQnlHbGpTSGRMUEphYgpCZi9HejVTUkJsclRiVWFuY2pQQ3NxdWZHc3kvdG1MUjV2cFVPWnM1TCtDMFBvVSs5NUlXV0lya1JCYndJMlRRClNOemtGRjdkVVR2MFZSVno0M2ZvNUIwRGU4aFBUa3B5WU1IbWZrT1lSMW5qQmpKSThFRDlzR3dMRDZ3Y0JONmoKbUk5YmxjQ0VDZ28yTStkYzlTZVJKY2crVkgrN3VEZU5TREFPRGd1a0wyd2hjZVRpYStJWTJsWG5qcGkrRFEwMApVNHBhS2ExdkhBOTE2QVJRZFkya1ZXbGV0RjJ3ZGFuTGMxcnlQdEd5bnEweCtQK2RGK0pXejV3Uys3ejQ0b0QvCllud2daWlJvQXFaVVN5bm0rN2NNaExQWEJzckVVcmtCS1JjbVlIYllPdHNRS0F1TXJvUGNJWnN5VkhDcmZqelEKOUYrTGYwUmJUY3JyREN1STN2Ti9oc295SEY0K1lnSWx2SUVuZ2RzYk5SSkdyMTJtVUh0NjhqYThuOUpuY2VobAp6eVZQbW9WRXNnMGs0TmtSS0pjT25Sd04wT20vYWdGZG5xYklMRTNQUDFUQ2JramdHd2hMd0VSSFlzODRTUnhSCkxDbE10WFZ2STBzRWcxTldwdWcrR1ZWUElqOGxOcVZ1WHY2RFBVRVFNVmNWenVobWY1T1B4Z0F1MEZIdVArZWIKRGlCTG9RWnVxOEJzWHZmRlZROVVpN1Q4UUFYQUNkVmFuQ3U4ODk0L21xKzJMTGtYT05IdHV1R29xZmRDT3kvbwpKZ1hWM2ZGdUZvVUJuU280bGk0VHU3L3J1cG84Z2lZVyt6bWZ3YkkyUzhWSlRVcjk3QUwzNHBMQnRXVStndldICmZvTkI0eEx1VndOMDdsU2dOd3JkWVJOK1NQQ1pETnVOaE5EcFRTWm1PZWZEb3hIQndkNjd6dGJhRjRaakZJaysKd3NsZGtRY3hEVFVaUG05L05vVzkrL3d1Ylp5bmU2ajkyQzAwdmNQT0c1S24wZFBESEF2T0x1MFlZcEphL0xvSgpYRCtHa0I0cFNyR1VQY25lOVZtZmxjbS9OUmpkSHpUbHJ5bGNkU1B5bnozSDVldzQ3OWdCYXVocytSblhSTHI4CmJXUnNNUEp5NXRPTHY3ZFZzRzdTK2REU2ZOY3FzSktjZWpDWjliR2NNU2tLcEdlNW9taGQzU0ZNWnhvQ3kzQVMKS2tkeUY0UklrbnlZRUthS0kyNEI3U05lQ0M1NE1XYXdXUHUzQzd3djNoZnZwS1BLaTkwUGhoZjJaTVA2SzlrbQowM3QzQ3pIY2l5K3FIQXp4YkZmN1lZT1pzQ0dLT3I1cWkxaUZta1h4MHFjYXMvT1MvTTljdTdsQ2s1bkJxUkFwCmZ0bTJmd2pEOUJ6Vm94MCtwbFNpZExyWDBMNnptWlVYZlBSSUwwbmxlL3p4ekhXeHJlU1A1TFVQc1ZuVG1iWkgKQmJ5d2ZSQi9hVDRvWkd1OGY1ZTR5NDdUclgzRDRTbFRObVFQMUZ2aHpiWnphdFNTRzJEbGJ4cy95RTEzWFpwMApNUHhjVzliVEU5MzF4YUNlenFSaWNjS2VTeEJxdDUvTlVQamlSdWxldTVIU2JHWGx6UmFZaHIxeEZpWEk0UkQ4CkNxSjFPYWxZS21GcHZXeUdtTm0rOE4xZGlmc1lvU3FkSkJBeU10M3dqVlU5OWpsZEk3Vy9zWDZ0QkpsV01mVUsKSEtLVVFNWThyMlEzclZwc3k4VGdTb0RXcHA1YytIa1NCcjR2QUhVVE1ybktBN0hITS9XRk5wVHpCb2ZJcFBjbApwSERySGlRallWVXp3amZDTXdVOUlIbTgrVVJNZ1ZQbjVxNytyWDN0eGZpdWpQcklhRUQxSjVRYnQzWkxDL1RaCkM1YVhKbW9rM2NFSFFRS1ZMVWdMVFJ2dDRaR204MVFZYUlqdHZQN1lyWjhnZnk0c2FucVNlTG9vT2VrQWlTdC8KUWovYmUxV0hsYzBVNzNrWnlDU2tFbEoybS9pUEZDNDM4NWVUWHN0RzVZWjBXM2srRU5yWVo5UzBGWE5sWTNWeQphWFI1UUhsdmJIUXVhVzh1ZEdWemRJa0NKd1FUQXdnQUVRVUNYcnJKRXdJYkF3SUxDUVFWQ2drSUFBb0pFT3pICnlYMi8rcTd5Y2kwUC9SblQ0c3N5N2FCb2I3WE5LM3RLb3hwVG5Udk84UkFvTmRVL1FZclFPc2hmcVhrNWdPYnkKSmplM1hOVGxmZ2VMbVE4ZFh2Z2VDbE1QUXNzcE9taERMTlR4ejlKUWRYT1FSRW9FdE1wcW1SSENXZHo2ZXRSZAowdUpQaU1Ib1I3bjJvR3o0Z09ud1pKTE9Wc01SSmw0eFNxV01rSFhpUFBnbHpubVRuL1ZyQVRrbGdUS1pUUnZVCk9wRGZIdEJhMzRKTU9QUkRORHFub1NUbUdFMStsYmxsekVjRzFId3RKWjk5Qy9idHVETFNQMnVPRTZXdkx5UUIKVWhlRzV0Wmt5L0w4RzhGeldsT1Z1S0MwcC91UkpuVzhhTlJXQnhBK0hjQU1kQ1FTSEtubnJVcEc4V2lNZGNiUAplZHdMOTZiSXhzT1orRDlVMS8wNW5Yek5kU0Rnc1RUbnNKVk0vbUZ1RnRHazEwM2E0cC8ra3B1MWVyU3ZzYTN4Ckw1UVQxeWU3RlJiYytQU2VYRVBxOWZFZTdGd3FjbDB2S0lLQ1hOa3ZPaVdIelY0WEgzbDNCYTVQMncwNjMyUG4KZmJCUGtwU2huUjFRVHk3akRnNkdqSDRoRTBwZ2NtNWhMU1FwVTUrUzE2eVVZT0YrN3RNVWdqa3FSOTFpM2hnKwplS1V1ZVVRaG9WL3I3ZWlka2xUZ241T21uc0JEUnJJaERRQlJ6dXdTbnYvVFZRSVVzRWFoQ2N3QXU4UmlTOXIzClFJUk53ajN2dDFscjJmdm10V2FvN3dmMzBtY0RUT0pwZWF6MmdZc0FsdDVFZjRoaTZuZVUyNDlYelFiVnZ0bXEKa0VqZmMwZDFQZ2VLVEVJY0xTeXBxODF3UVpDUEFuRnRlQW1sSDFxclFQcFlNdGRxVTRubk1LdGRuUVBHQkFBYwo1RkFDQ0FDc1Z3UjhITmJ4WUdndnREcm5WL094WndhMUM3K01vWEJxNXZsd3ZBeldVVmVkTnpCaWNRRERFcHk4CkM3OVlRNCt2cVozSlEyaHFHWmIzdG9Od1RNVXpkOURiclJaRzhqZzRLdGE2d0x6SEJiNjVmZXllNW1vMnRGdSsKb3NrYTBNcm02L3hCQ3NYZHNVTUx1RzZuRmNQdFV1Y0hLRWJldzdDTDhMUXE1QVNVQXRqeENKWlhsZUxDZTFERAp5QW1rK0taUE9iekxwYzFPMUQ0NWcxQ21LMlVTeG82QTF6U0hLOHZ5WCtGNjRKd0pnWnFIc1ROYXE0OVlkSVZTClVJM2wydE9ITlJyQUFta1FLMVhNdkw1WlBlQmE3eW40ZHJhVDAvc3huWUhla2JKWVhtVVBJUHl2dlV4KythSzAKL01DOVNMS09PMnpRVlNCWnp4SzJ2TDRQVE1ZakFCRUJBQUgrQ1FNSWhNT3hkMm5RN2FYQVZkNHBlTGQ5T2xKUApUZkk3UDg3b3dTYld4bVpmM09uUFBic09aSFpoZWppUUUwbjRYbDZ1MlFLTXl5Y05iRmI2d2toeGZYYTBKWS95CkNUNzhRV0JiUXhaNTBOZmcyeno1WktPM1VvQkkycmhaVG9VcTVPTHVyYXRUTlMrQk5ha21ocWZYa1UyVXJvZ04KVTJxcE1CeC9qTlc1UDhZNXpOREFEMUFDUWRjTkwrblkyRFVvY0o4bWN2Tm01VWVOcitFUDRCdzljSXpIWkIvZwpBYWtnbjlVUlJROTcveHErc09BZzlxbkJSNnJmWVJ5ZFEwejlsekFlb3VSdnRTN0FSbzB3SStjNC9UUzFJQjVaCjM2dUNCWGVjdkMrUjFQNkJFTG1VN2lUaUF6UTVqeFIxTzV4UDJpNmdsbW9pT3EzVjBGenB4L3BjVGJmbzhPZTcKUThWaWQydFpzdG0zeTlDcXhHTmpWd3FiWFU1UTlHem4xZkNjbUNreFlISStwRlY1Wk9oRFRmdmljWE9ON1VKNAoyWDFGNjB0ZHRLSU8rZTlHd2wwczFwUXFsZnlEd2hzWm14aWhBN1R2WW9CUG91ZERVcEVvbzV4clB4a2lYRVl4CnJtNXRBMkkrTTJtVFRHUFZ1VVg1b1BNZHVOQ3RaOVVPeWJzYlFhd1pyb09KdmhLTnA0UEhib1FPWko4T0Z2ajQKR0EvNlJEaUlTQS9uTHFoOW1tVkMyVXJydGhRdGlNQzNxMUppMnlPbFFManI0SXhqUzFCYTRNY2lBNU5qclQ4aAorRDZMcTB5Z3pzOFppUEEzNG1QeE9tREtEYjUyMnRkU1hPcFIzS05Sczg1enJFbk44ekRySUhHaXhIcVd6clZtCjlrVGVwcXU4ZkFjTXlzVUMxZEZnb2kvRkluSWxVYVluTGhIeGluc0JZRDJySHUyWTZVUFRmUEtlVm1qaDVOdUYKSVliYXRDMGpCS2lSUW9TNGkwOTZUaHdRZWVEZ044aFpSSnl4SmVscVBzeDhnYmdGKzJHdEJVQzduRERGQ0V4bwprUnVtYWowMm5jbEQvcW5qL1lRbjJzQTRaTWx6dnV2MGhJbEd3dUMxTEJKZWJuOXVHeEJRakZEZDk0RkExbm90ClExRlVHM0VYWkd2UnZ3Y2VZbS9ZRzUybytoT2s2UUlPT0VEdUw5ZmpZVERuSDFnRkFUbUZkeFJLa2llWngwUGYKKzRrQmlRSXFCQmdEQ0FBVUJRSmV1c2tUQWhzTUJCVUtDUWdDQ3drQ0ZnQUFDZ2tRN01mSmZiLzZydkl3MUEvLwplWXZ0eWI3NFJiUE5HckZRa0NPcUJLMDZPcnA0Nnd0LzBXU1R5SmY2VU1zdlp6VDNoOUNMNUNsdDErdHJSYVBrClQyaHlsb1BMVXk3TEpyL0p0dEVzQUNVYTM0d08zdnlTbWlLRzJDMStQbEJ2T214dHhnR1gwYk96MHJmUjJTQTgKMWcvTitSVndndkhaVWs5aTF4UEx0c3VJODRJTXgza1hrcWgwMytQMmluY2JGdnZvMmc5d1RBWVlOdjYwQ05DSwpKaXlQVHBEYWw2ZmxEMGtzNlJReHd5Um5jZUZXMm1tUXdsVEdoQXpyQm1oaEowMHh4ME1yV1ZKU05IbFhWK1RaCnhBdndXN1dnQlZLUXU3L1BWZnhXUEt0NWIrakRvWWpSZ2hzWXdhLzRtU3dkSzIva2RPQ0QwR21pWk9hSnVOaWoKSm9HSXN1OXUyMnRicWhQcHl3aEFXZEZMbHdSaFpyOHNBSmxjOERXVXlOVlcvaFZuTE1LdDc0U3VsQ2JmSkFxaAovZmpxQTZua09nb0hQN3B2UXNzTDhHdWo2cVdMcGxyWHZiekFGK2d2N3lYUW9CY0tMWkZRVjM0SVYvM3c4YitxCk1FOUNDWnpMY2FUNjJPWDhleXU5QVpXRkZGeGZLeldyY0h1WXlud2hRTkUyZXllcjV1eW9TTGszYUg5MjMrSFgKMVpIVjFFN2VCR3Jrc09NYTlPblBDc3pHaER0U0o4S1R0RDRNWHlEYXE5NjVjSVVoOEdzS2hoeFZZZ3hqaEN3OQpIcU9tK1BFY0toT3ZzVkFTMTJtdC9uVTlobTVjNmEvOTlxd0pmR21xYklFT29SS2xTWG9jUnh3d2JYSkxQNkRGCnZYNDU5aDBJRHJaQWpLYlc5cllrSHYvQWpGZkJCT2ZBWU1VSlhpam8rVjA9Cj1hMHpiCi0tLS0tRU5EIFBHUCBQUklWQVRFIEtFWSBCTE9DSy0tLS0tCg==----------\n" +
                "LS0tLS1CRUdJTiBQR1AgUFVCTElDIEtFWSBCTE9DSy0tLS0tClZlcnNpb246IEJDUEcgdjEuNjMKCm1RSU5CQUFjNUZBREVBRExkaHdvNElBaTF1RU1BRzQvd0pzSGJ6QVF1WU5WeFBBcHVDOWdJNnV2Nkd2dzVvc1MKS3VGZzB5UlY1YXpqWlcvNnExTVFheDRPSHozMGFvMjZ4ZGJpMjNaOFdsTWQrWDV3blNJRmk0Vng0dzNOWXRwVwpVdnB0Z28xNUF2LzFmWVpiRFdHWmpMSDl2b1BtY3FMKzRzemJmdGpzNkFwYjhEbzZqOVdVUnlYd1h3T0kzaHBaClBVOU9acGFxeXFaaWRLZnRHN2JvYUFlU3JLODh3SDNqWHVDbVQ1a2FQcFhhczBoUFZVWlRHUExjWlJiMFF5WGYKbnJpY3RKSnhTZ0RvTE0yUlZtd0F0WFptc2NLaEVuNDUyTCt2NERWbkdzMmxwWHBpcnlGalRzSUU0UlFtSGREMQpuMmlkVzdra2pZeHo3NFpVdWw4ZlE2QnJwU3RNa0pYaVVqSytEWWtaOUpqb3czb0dCQWhvRDFEeXVMRTNJRTBBCkRrTzN1eUFkMDd3YUhUZmovNmpadEJuU09lZHc4aWw0MEhxK1N6Z2liSExUbEtyOURUb3I0TmRwcU9tQ0FGOWoKeENoWS94QmJ5Uk1aWFkvSHJnaEh1c1hHTkxOL29vbUpTall0UjUvNjA3cWFrT3lIWHlnS25WS2tET0lHN2RyTQpHQXJpQ0RDVEVzQUJzcTVCRU10NHRxR25lTlBwdmk4S2JWelhBWnh0RUU2Q2M4dUliS3ZTOXZ2b25QOG96bUF2CnRDSXJaYWhYQTRzVk1MRDB6OG9NOHFRblB2MEpYUmRGMmtsSkVlQUY0V1RteWxlTVUzWTIyUlRyN1ZwWVRQcEgKWTUzYWZmU1JHSzdDTTZLbng4aDNsd2JuQWFyUXA4YVNwa1J2cTY3VlF3emVraXBaZklNbUhTWGxwd0FSQVFBQgp0QlZ6WldOMWNtbDBlVUI1YjJ4MExtbHZMblJsYzNTSkFpY0VFd01JQUJFRkFsNjZ5Uk1DR3dNQ0N3a0VGUW9KCkNBQUtDUkRzeDhsOXYvcXU4bkl0RC8wWjArTExNdTJnYUcrMXpTdDdTcU1hVTUwN3p2RVFLRFhWUDBHSzBEckkKWDZsNU9ZRG04aVkzdDF6VTVYNEhpNWtQSFY3NEhncFREMExMS1Rwb1F5elU4Yy9TVUhWemtFUktCTFRLYXBrUgp3bG5jK25yVVhkTGlUNGpCNkVlNTlxQnMrSURwOEdTU3psYkRFU1plTVVxbGpKQjE0ano0SmM1NWs1LzFhd0U1CkpZRXltVTBiMURxUTN4N1FXdCtDVERqMFF6UTZwNkVrNWhoTmZwVzVaY3hIQnRSOExTV2ZmUXYyN2JneTBqOXIKamhPbHJ5OGtBVklYaHViV1pNdnkvQnZCYzFwVGxiaWd0S2Y3a1NaMXZHalVWZ2NRUGgzQURIUWtFaHlwNTYxSwpSdkZvakhYR3ozbmNDL2VteU1iRG1mZy9WTmY5T1oxOHpYVWc0TEUwNTdDVlRQNWhiaGJScE5kTjJ1S2YvcEtiCnRYcTByN0d0OFMrVUU5Y251eFVXM1BqMG5seEQ2dlh4SHV4Y0tuSmRMeWlDZ2x6Wkx6b2xoODFlRng5NWR3V3UKVDlzTk90OWo1MzJ3VDVLVW9aMGRVRTh1NHc0T2hveCtJUk5LWUhKdVlTMGtLVk9ma3Rlc2xHRGhmdTdURklJNQpLa2ZkWXQ0WVBuaWxMbmxFSWFGZjYrM29uWkpVNEorVHBwN0FRMGF5SVEwQVVjN3NFcDcvMDFVQ0ZMQkdvUW5NCkFMdkVZa3ZhOTBDRVRjSTk3N2RaYTluNzVyVm1xTzhIOTlKbkEwemlhWG1zOW9HTEFKYmVSSCtJWXVwM2xOdVAKVjgwRzFiN1pxcEJJMzNOSGRUNEhpa3hDSEMwc3Fhdk5jRUdRandKeGJYZ0pwUjlhcTBENldETFhhbE9KNXpDcgpYYmtCRFFRQUhPUlFBZ2dBckZjRWZCelc4V0JvTDdRNjUxZnpzV2NHdFF1L2pLRndhdWI1Y0x3TTFsRlhuVGN3ClluRUF3eEtjdkF1L1dFT1ByNm1keVVOb2FobVc5N2FEY0V6Rk0zZlEyNjBXUnZJNE9Dcld1c0M4eHdXK3VYM3MKbnVacU5yUmJ2cUxKR3RESzV1djhRUXJGM2JGREM3aHVweFhEN1ZMbkJ5aEczc093aS9DMEt1UUVsQUxZOFFpVwpWNVhpd250UXc4Z0pwUGltVHptOHk2WE5UdFErT1lOUXBpdGxFc2FPZ05jMGh5dkw4bC9oZXVDY0NZR2FoN0V6CldxdVBXSFNGVWxDTjVkclRoelVhd0FKcEVDdFZ6THkrV1QzZ1d1OHArSGEyazlQN01aMkIzcEd5V0Y1bER5RDgKcjcxTWZ2bWl0UHpBdlVpeWpqdHMwRlVnV2M4U3RyeStEMHpHSXdBUkFRQUJpUUlxQkJnRENBQVVCUUpldXNrVApBaHNNQkJVS0NRZ0NDd2tDRmdBQUNna1E3TWZKZmIvNnJ2SXcxQS8vZVl2dHliNzRSYlBOR3JGUWtDT3FCSzA2Ck9ycDQ2d3QvMFdTVHlKZjZVTXN2WnpUM2g5Q0w1Q2x0MSt0clJhUGtUMmh5bG9QTFV5N0xKci9KdHRFc0FDVWEKMzR3TzN2eVNtaUtHMkMxK1BsQnZPbXh0eGdHWDBiT3owcmZSMlNBODFnL04rUlZ3Z3ZIWlVrOWkxeFBMdHN1SQo4NElNeDNrWGtxaDAzK1AyaW5jYkZ2dm8yZzl3VEFZWU52NjBDTkNLSml5UFRwRGFsNmZsRDBrczZSUXh3eVJuCmNlRlcybW1Rd2xUR2hBenJCbWhoSjAweHgwTXJXVkpTTkhsWFYrVFp4QXZ3VzdXZ0JWS1F1Ny9QVmZ4V1BLdDUKYitqRG9ZalJnaHNZd2EvNG1Td2RLMi9rZE9DRDBHbWlaT2FKdU5pakpvR0lzdTl1MjJ0YnFoUHB5d2hBV2RGTApsd1JoWnI4c0FKbGM4RFdVeU5WVy9oVm5MTUt0NzRTdWxDYmZKQXFoL2ZqcUE2bmtPZ29IUDdwdlFzc0w4R3VqCjZxV0xwbHJYdmJ6QUYrZ3Y3eVhRb0JjS0xaRlFWMzRJVi8zdzhiK3FNRTlDQ1p6TGNhVDYyT1g4ZXl1OUFaV0YKRkZ4Zkt6V3JjSHVZeW53aFFORTJleWVyNXV5b1NMazNhSDkyMytIWDFaSFYxRTdlQkdya3NPTWE5T25QQ3N6RwpoRHRTSjhLVHRENE1YeURhcTk2NWNJVWg4R3NLaGh4VllneGpoQ3c5SHFPbStQRWNLaE92c1ZBUzEybXQvblU5CmhtNWM2YS85OXF3SmZHbXFiSUVPb1JLbFNYb2NSeHd3YlhKTFA2REZ2WDQ1OWgwSURyWkFqS2JXOXJZa0h2L0EKakZmQkJPZkFZTVVKWGlqbytWMD0KPWtQRWQKLS0tLS1FTkQgUEdQIFBVQkxJQyBLRVkgQkxPQ0stLS0tLQo=";
        Path createdFile = tempDir.resolve("stubs-example-key-gpg_gpg_pair");
        Files.write(createdFile, Lists.newArrayList(TYPE_INDICATOR + "gpg_pair"));
        Files.write(createdFile, keypair.getBytes(UTF_8), StandardOpenOption.APPEND);
        Ring keyRing = new KeyRingCreator(tempDir.toUri()).createKeyRing().getRing();
        assertEquals(2, keyRing.getKeys().size());

        testPGPEncryption(keyRing);
    }

    private void testPGPEncryption(Ring keyring) throws IOException, PGPException {
        Encryptor encryptor = new Encryptor(keyring);
        Decryptor decryptor = new Decryptor(keyring);
        ByteArrayOutputStream cipherTextBuffer = new ByteArrayOutputStream();
        encryptor.setCompressionAlgorithm(CompressionAlgorithm.Uncompressed);
        encryptor.setEncryptionAlgorithm(EncryptionAlgorithm.AES256);
        encryptor.setSigningAlgorithm(HashingAlgorithm.SHA256);
        ByteArrayInputStream inputStream = new ByteArrayInputStream("test".getBytes(UTF_8));
        encryptor.encrypt(inputStream, cipherTextBuffer);
        org.junit.jupiter.api.Assertions.assertNotEquals("test", new String(cipherTextBuffer.toByteArray()));
        ByteArrayOutputStream clearTextBuffer = new ByteArrayOutputStream();
        InputStream cipherTextStream = new ByteArrayInputStream(cipherTextBuffer.toByteArray());
        decryptor.decrypt(cipherTextStream, clearTextBuffer);
        assertEquals("test", new String(clearTextBuffer.toByteArray()));
    }

    @Test
    void testPGPSingleKey(@TempDir Path tempDir) throws IOException {
        String pgpkey = "-----BEGIN PGP PRIVATE KEY BLOCK-----\n" +
                "\n" +
                "lQPGBFyKMyEBCACwIgX6oYBxHasN6irUhxY0QdO+veUQmElH07WqwLhCRNo6mLBX\n" +
                "Oi/OBIBBUN/w36LyR0YVovk2fCfA2WaeHHkLeDKo1HoHIz7ByrV03Jlp7aQkV3G2\n" +
                "z9D2fEGmFnbJ2mclh7bDKsQPJkjL2dsuYrlCzNUCw3m1Jf2HhW1q6JkU2JpHfmW+\n" +
                "ho+9ZIBWn1F4yJXYot5ziSvDjiJjcLTK26unNfB7C50wdwpSz5IlV6h7pW4XL44d\n" +
                "a/q5QeUp8Swgu736m0ahgwkX2DpxwoZSBGGVBYaElE1W157H/Q8sDi3BBvEe49mZ\n" +
                "j4+F1mNw3ijmdAAHreu7jdC2SVlDbiSsR6stABEBAAH+BwMCloTWoxEIK2Xpwz+v\n" +
                "ztCf7YPmIj0S0bcEvox24a+MdUj41DdNoqNMvFFybEedeOgTjzHNiDTPVWDTTAjh\n" +
                "131iilL54oe+YsO7DhcXiTGT2GhPZoGonl4b7Vmotx50WsnO6GfNivZoLubsUhDA\n" +
                "45SfoVpWrbJdakW/Cqz8d2LtxtXfKt8LPk84pfE5S2PHAyVF+1mOciXHfhxQ6Cpj\n" +
                "9yuQ5Ymf249TaCqBMgy9oZkFCGn49gowDxc4F6mtPNy+YZ6LtLUauYZkSnjPu2C6\n" +
                "lor9tbSzIhjMWux67UjrDvdzkbzqS/PRUQHjP/CXD9l13ZJ2Fzbu7xH2XsN5AXpP\n" +
                "ctWF32b+E7KPJGXztZSjsb8FL7eGQhK8s9CgULhYbN095Xo9Aa9eYISZhyJDV4z5\n" +
                "3QZMhJHwUT+ReH1PW/tizklAYt5hDUksX7aDH5gk5us1uQOdb01abEAOkNZYvb+l\n" +
                "KnqqXK51y+bP/H9mumyN4jV82QRQKSq6Nt5e8g1qQyisvxPTBK9Bbr0XFTfT7Zxq\n" +
                "KOK3zaqh/PQMRs9tkoi+/gTWAaCAxAuGGEleZTNfEmSjejAezxHnTwVPFiAEUD5M\n" +
                "5p+TtuLh9QYBYnV/qYmGXMt+b++v1NCFxJaaiqDARw3yBg7axsinD+RnQmE4xFMK\n" +
                "X5xeKbKbVUThsjMJvhHDYdFLpd6W4rz3SxzUCC4F0uczqKuksgXh+oQWwy2PA8LE\n" +
                "hOo6ELYjUm43MDmJdqlQQlUvFueszhJ3q9iNSmuLoBXmjPRaXxFY531nYNM8Dorz\n" +
                "E6cS3E1HUFBUNkhqNRQRxYGiLGOjq8RV4dzsg4ybdRTrF+XDCc9fZozd5b3bauye\n" +
                "a7UXc70nQPoxHSR2K2UaZSx2DrO2B7R81T8Cqf7D9yOXFB2Mh4mAx94GKvddfCe0\n" +
                "FIs2y10whwiDtBt5b2x0LWt5Yy1kZXYgPGRldkB5b2x0LmNvbT6JAU4EEwEIADgW\n" +
                "IQRRdsM6LZM762bbsEL19wy1pogHpwUCXIozIQIbDwULCQgHAgYVCgkICwIEFgID\n" +
                "AQIeAQIXgAAKCRD19wy1pogHpyWqCACERD3PbEMVDHSCaIymGqiQ3/nI5i/GvQL2\n" +
                "PMwCxs0DEyFni0++QMKwnrhdC9BZh27JbExQmcgIOLS0THfIjXZlUb4uE9dnseXJ\n" +
                "6/A01BhOhbRSXkjTv9Iv7pGn0Zsdl7EQ3NJsYl6lqRa3bWAfMfx4zDvqIeHDYOqT\n" +
                "DOtO2C8RRUh8u9lSBnSMgAcNiO2BqmWrR4sdyU5vGfV8eltl8KwVhlo8NwUUZAzm\n" +
                "ZSvQMGD4GbUJf3i6TmX82z2mWAJtAWeDrKkfSfj7bX67j7asmId8VSauYniSd7QG\n" +
                "xEqOIL6a0WK3DRkWNb42qIDc0ws0VhNbq8OVOTVYIG3XripIUnun\n" +
                "=wlog\n" +
                "-----END PGP PRIVATE KEY BLOCK-----";
        Path createdFile = tempDir.resolve("singlePGPtest_gpg");
        Files.write(createdFile, Lists.newArrayList(TYPE_INDICATOR + "gpg"));
        Files.write(createdFile, Base64.encodeBase64(pgpkey.getBytes(UTF_8)), StandardOpenOption.APPEND);
        List<org.c02e.jpgpj.Key> keys = new KeyRingCreator(createdFile.toUri()).createKeyRing().getRing().getKeys();
        Ring ring = new Ring();
        ring.getKeys().add(keys.get(0));

        assertEquals(1, ring.getKeys().size());
    }

    @Test
    void testPGPDoubleKey(@TempDir Path tempDir) throws IOException {
        String privateKey = "-----BEGIN PGP PRIVATE KEY BLOCK-----\n" +
                "Version: BCPG C# v1.6.1.0\n" +
                "\n" +
                "lQOsBF6xyw4BCACxzh/TtWop5PMNDT54CyV5p/hwBjzl3XE32xNwRX41JqjGn5ZD\n" +
                "osUUooPkwdjgV3qnjDBPqi/D2cSVqxn45hDosATRk7wxley6koctHwUsLfWBw1Ys\n" +
                "GlM72KgY+PuVsGlZqUljTkdzOJy/BZmSiBZgOtMPxyZuv4ZF60YvYOeqkDNgBcx9\n" +
                "12brNAdRokD2AigZHLvngfoPKmof7OHwSzzdYJaTCzwJjSBsLKrN+Iw3z0XO42Rj\n" +
                "zhIB3PhUnoFcefYtyeTfw56YCnh1evNiY3jRC7PHmyMorn+Po1SQBV7tkWZTvvZ9\n" +
                "mKFN0JhvbNa6Cc1ZLZJhfGf9MrsUyFK4cxgfABEBAAH/AwMCpgl1kGXvBIFgyQ/h\n" +
                "bG6NFbtvg4L2TCQGAcZqFY06Zi+FfgnlqoIc7LgTFXCNW2+qUEOsTW3Khb8Uo9HE\n" +
                "0ps0giA3oXCd3N4LT5DIfOK+aCfpsO2HSr0+do7Poa/+TxrxIVOBkrpSh57hRA2P\n" +
                "ZPjzS7Ibetmj+p3jaWu8pMq0JqwI7Fu6vVhzclvfK+TtAuC1DxkppkJDLPwv9yT6\n" +
                "ZHogPhznONRzVEtZ7c4F1zz8OZno3j3dr/IM3xit4qQiNhV0vZ3oRy9/pTBLq6TS\n" +
                "np/mu6cJpU9a/2+SmghjWRcc87snKgjUFGOJvYwSFezRDCUodYmtVGsw7aGD3a1H\n" +
                "0ZH3JJH19wn8RAKnO1TjsKz1J4rN0QoWI2GLwG+Gryj01ZjcyTqaixlm6Gl+2Bn2\n" +
                "bigHV9YIPBeY8LOvkIh/BPQCyXxE1UKLDmu2le+0pnfLbM0+KaPW9ryLsOG+Kh4b\n" +
                "XPmasmrAKRpOIQKrhI4IzV7EjxdGb3v4R6/hlt6ceTOg2n5rgm45i4sJHkRiXgAY\n" +
                "YKsIVU7nLTer4cCDbikIdwFTrzAsgUXWgwaIHQBKqbkxk2LEBqTbc6AmMJEJbZYA\n" +
                "LfuGpVE4P5/bcKcJ0yYCLW0VHDBYo8u4o6sFnq8h939pvljmfhWqWQpKciSX8rcc\n" +
                "bOkRhW5gw6xdyA5dOBCQCWCS1Yrg3QxWLdl12p9G5kWq0PQxHXYPLLDUvke1VHWp\n" +
                "oWfRlq///meFlVjuiZZP2VDcnmxf8a4F6O9/D0R6hv0FKg9i1nbVq5N6DZxRP5xX\n" +
                "HbfsarqX/avt9wtyn93kYdyiKXMAc3x0lFgAtNQsJ5veLbIV+p3p8hqPtiJBKym2\n" +
                "WnS6hToGmFXzYW3IYhTdkNAtHDyeFeTM9QzBoCs+77QMdGVzdEB0ZXN0Lm5siQEc\n" +
                "BBABAgAGBQJescsOAAoJEMms1lkiXEzBtaQH/3nEBZ+Iy8MPof/tIxA1xE9yjzeR\n" +
                "Pti8QLaGF412dSfR5HTU5j1C4Ox/iXWc49KcTeJXgVfGhwDBY0jf0GmHWH1/5Nmg\n" +
                "Bf0HGYpNyHXAUFCNvM6XFzlnsjcFgvrYboHKtQ2xG9cHcOWM8uDAWXFJ5TB2PiIw\n" +
                "IZcnb7/2hauiW9b4DG68DNT3G4uExzuGCMe+JuZyIVTYLRaYkkSjQpMtPMLGKX55\n" +
                "8hMIaRl7Z2zC61tP8Fc1OGt3OFR62SLBRFkjIv0YvbqXvgSRssU1OaOaWz5n5lBq\n" +
                "d1vOYijFzyI9GF/AuvW44jBcl+hGL6eV7aIaoTlMkG4lqICwdb7wKTkgjzg=\n" +
                "=mVXp\n" +
                "-----END PGP PRIVATE KEY BLOCK-----\n";
        String publicKey = "-----BEGIN PGP PUBLIC KEY BLOCK-----\n" +
                "Version: BCPG C# v1.6.1.0\n" +
                "\n" +
                "mQENBF6xyw4BCACxzh/TtWop5PMNDT54CyV5p/hwBjzl3XE32xNwRX41JqjGn5ZD\n" +
                "osUUooPkwdjgV3qnjDBPqi/D2cSVqxn45hDosATRk7wxley6koctHwUsLfWBw1Ys\n" +
                "GlM72KgY+PuVsGlZqUljTkdzOJy/BZmSiBZgOtMPxyZuv4ZF60YvYOeqkDNgBcx9\n" +
                "12brNAdRokD2AigZHLvngfoPKmof7OHwSzzdYJaTCzwJjSBsLKrN+Iw3z0XO42Rj\n" +
                "zhIB3PhUnoFcefYtyeTfw56YCnh1evNiY3jRC7PHmyMorn+Po1SQBV7tkWZTvvZ9\n" +
                "mKFN0JhvbNa6Cc1ZLZJhfGf9MrsUyFK4cxgfABEBAAG0DHRlc3RAdGVzdC5ubIkB\n" +
                "HAQQAQIABgUCXrHLDgAKCRDJrNZZIlxMwbWkB/95xAWfiMvDD6H/7SMQNcRPco83\n" +
                "kT7YvEC2hheNdnUn0eR01OY9QuDsf4l1nOPSnE3iV4FXxocAwWNI39Bph1h9f+TZ\n" +
                "oAX9BxmKTch1wFBQjbzOlxc5Z7I3BYL62G6ByrUNsRvXB3DljPLgwFlxSeUwdj4i\n" +
                "MCGXJ2+/9oWrolvW+AxuvAzU9xuLhMc7hgjHvibmciFU2C0WmJJEo0KTLTzCxil+\n" +
                "efITCGkZe2dswutbT/BXNThrdzhUetkiwURZIyL9GL26l74EkbLFNTmjmls+Z+ZQ\n" +
                "andbzmIoxc8iPRhfwLr1uOIwXJfoRi+nle2iGqE5TJBuJaiAsHW+8Ck5II84\n" +
                "=vvWc\n" +
                "-----END PGP PUBLIC KEY BLOCK-----\n";
        Path createdFile = TestUtil.createAsymmetricFile(tempDir, privateKey, publicKey, "doublePGPkey", "gpg_pair", null, true);
        List<Key> keys = new KeyRingCreator(createdFile.toUri()).createKeyRing().getRing().getKeys();
        Ring ring = new Ring();
        ring.getKeys().add(keys.get(0));

        assertEquals(1, ring.getKeys().size());
    }

}
