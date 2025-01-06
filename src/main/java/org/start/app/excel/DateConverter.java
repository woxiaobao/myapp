package org.start.app.excel;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.converters.ReadConverterContext;
import com.alibaba.excel.converters.WriteConverterContext;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.data.WriteCellData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateConverter implements Converter<Date> {
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Override
    public Class<?> supportJavaTypeKey() {
        return Date.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public Date convertToJavaData(ReadConverterContext<?> context) throws ParseException {
        String dateStr = context.getReadCellData().getStringValue();
        return new SimpleDateFormat(DATE_FORMAT).parse(dateStr);
    }

    @Override
    public WriteCellData<?> convertToExcelData(WriteConverterContext<Date> context) {
        Date date = context.getValue();
        return new WriteCellData<>(new SimpleDateFormat(DATE_FORMAT).format(date));
    }
} 