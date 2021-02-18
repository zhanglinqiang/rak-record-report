package zhang.service;

import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.images.FileImageProvider;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;
import zhang.model.ImgInfo;
import zhang.model.OutputModel;
import zhang.model.ParseResult;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

public class RakRecordReportGenerator {

    public void generate(ParseResult parseResult, File outputDir) throws IOException, XDocReportException {
        InputStream in = RakRecordReportGenerator.class.getClassLoader()
                .getResourceAsStream("template.docx");
        IXDocReport report = XDocReportRegistry.getRegistry().loadReport(in,
                TemplateEngineKind.Freemarker);

        FieldsMetadata fieldsMetadata = report.createFieldsMetadata();

        fieldsMetadata.load("outputModels", OutputModel.class, true);
        fieldsMetadata.load("outputModel", OutputModel.class);

        report.setFieldsMetadata(fieldsMetadata);
        IContext context = report.createContext();


        List<ImgInfo> imgInfos = parseResult.getImgInfos();
        Map<String, List<ImgInfo>> groupingByPerson = imgInfos.stream().collect(groupingBy(ImgInfo::getPersonName));

        int size = groupingByPerson.size();
        int index = 0;
        for (Map.Entry<String, List<ImgInfo>> entry : groupingByPerson.entrySet()) {
            System.out.println(String.format("正在生成'%s'RAK报告(%d/%d)...", entry.getKey(), (index + 1), size));
            List<OutputModel> outputModels = new ArrayList<>();
            for (ImgInfo imgInfo : entry.getValue()) {
                String message = String.format("%s %s 第  天  计数：", entry.getKey(), new SimpleDateFormat("yyyy-MM-dd").format(imgInfo.getDate()));
                outputModels.add(new OutputModel(message, new FileImageProvider(new File(imgInfo.getFilepath())), imgInfo.getFilename()));
            }
            context.put("outputModels", outputModels);
            File file = new File(outputDir, String.format("%s.docx", entry.getKey()));
            try(OutputStream out = new FileOutputStream(file)){
                report.process(context, out);
            }
            System.out.println(String.format("'%s'RAK报告生成完成(%d/%d)...", entry.getKey(), (index+ + 1), size));
            index++;
        }
    }
}
