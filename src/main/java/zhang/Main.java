package zhang;

import fr.opensagres.xdocreport.core.XDocReportException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import zhang.model.ImgInfo;
import zhang.model.ParseResult;
import zhang.service.RakRecordReportGenerator;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
public class Main {

    private static RakRecordReportGenerator rakRecordReportGenerator = new RakRecordReportGenerator();

    public static void main(String[] args) throws IOException, XDocReportException {
        if(args.length < 1){
            System.err.println("参数错误, 未指定数据源目录");
            return;
        }
        String dirPath = args[0].trim();
        File file = new File(dirPath);
        if(!(file.exists())){
            System.err.println("参数错误, 数据源目录不存在");
            return;
        }
        if(!file.isDirectory()){
            System.err.println("参数错误, 数据源目录错误");
            return;
        }

        long start = System.currentTimeMillis();
//        String dirPath = "C:\\Users\\leen\\Desktop\\2021年\\2021年2月份";
        String outputDirPath = dirPath + "RAK";

        ParseResult parseResult = parseImgInfo(dirPath);

        File dir = new File(outputDirPath);
        FileUtils.deleteQuietly(dir);
        dir.mkdirs();
        rakRecordReportGenerator.generate(parseResult, dir);
        System.out.println(String.format("耗时: %.2fs", (System.currentTimeMillis() - start) * 1.0 / 1000));
        System.out.println(String.format("报告路径： %s", outputDirPath));

        List<String> errorMsg = parseResult.getErrorMessage();
        if(!errorMsg.isEmpty()){
            System.err.println("-------------------错误文件列表-------------------");
            errorMsg.forEach(System.err::println);
        }

        System.exit(0);
    }


    private static ParseResult parseImgInfo(String dirPath) throws IOException {
        List<String> errorMessage = new ArrayList<>();
        List<ImgInfo> imgInfos = new ArrayList<>();
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(dirPath))) {
            directoryStream
                    .forEach(path -> {
                        if (Files.isDirectory(path)) {
                            String person = path.getFileName().toString()
                                    .replaceAll("\\d*", "")//去掉目录数字
                                    .replace("肿瘤", "");//去掉目录肿瘤字样

                            try(DirectoryStream<Path> imgs = Files.newDirectoryStream(path)){
                                imgs.forEach(img -> {
                                    String fileName = img.getFileName().toString();
                                    String filepath = img.toAbsolutePath().toString();

                                    String format = fileName.replace("。", ".");
                                    Pattern allNumberCompile = Pattern.compile(".*?(\\d{8}).*");
                                    Pattern withPointCompile = Pattern.compile(".*?(\\d{4}.\\d+.\\d+).*");
                                    Matcher allNumber = allNumberCompile.matcher(format);
                                    if(allNumber.find()){
                                        format = allNumber.group(1);
                                    }
                                    Matcher withPoint = withPointCompile.matcher(format);
                                    if(withPoint.find()){
                                        format = withPoint.group(1);
                                    }

                                    if(format.contains(".")){
                                        String[] split = format.split("\\.");
                                        if(split.length == 3){
                                            split[1] = fixZero(split[1], 2);
                                            split[2] = fixZero(split[2], 2);
                                            format = String.format("%s%s%s", split[0], split[1], split[2]);
                                        }else{
                                            errorMessage.add(String.format("error format date '%s', fileName = '%s'", format, filepath));
                                            return;
                                        }
                                    }

                                    ImgInfo imgInfo = new ImgInfo();
                                    imgInfo.setFilename(fileName);
                                    imgInfo.setFilepath(filepath);
                                    imgInfo.setPersonName(person);
                                    try {
                                        imgInfo.setDate(new SimpleDateFormat("yyyyMMdd").parse(format));
                                    } catch (ParseException e) {
                                        log.error("invalid date format '{}'.", format);
                                    }
                                    imgInfos.add(imgInfo);
                                });
                            }catch (Exception e){
                                log.error(e.getMessage(), e);
                            }
                        }
                    });
        }
        return new ParseResult(imgInfos, errorMessage);
    }

    private static String fixZero(String value, int length) {
        StringBuilder valueBuilder = new StringBuilder(value);
        while (valueBuilder.length() < length){
            valueBuilder.insert(0, "0");
        }
        value = valueBuilder.toString();
        return value;
    }
}
