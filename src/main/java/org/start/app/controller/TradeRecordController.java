package org.start.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;
import org.start.app.service.TradeRecordService;
import org.start.app.entity.TradeRecord;
import org.start.app.model.TradeRecordQuery;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.io.File;
import java.util.List;
import java.util.Map;

@Tag(name = "交易记录", description = "交易记录相关接口")
@RestController
@RequestMapping("/trade")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TradeRecordController {

    private final TradeRecordService tradeRecordService;

    public TradeRecordController(TradeRecordService tradeRecordService) {
        this.tradeRecordService = tradeRecordService;
    }

    @Operation(summary = "导入Excel", description = "导入交易记录Excel文件")
    @PostMapping("/import")
    public ResponseEntity<String> importExcel(@RequestPart("file") MultipartFile file) {
        try {
            tradeRecordService.importExcel(file);
            return ResponseEntity.ok("导入成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("导入失败：" + e.getMessage());
        }
    }

    @Operation(summary = "生成测试Excel", description = "生成包含10000条测试数据的Excel文件")
    @GetMapping("/generate-test")
    public ResponseEntity<String> generateTestExcel() {
        try {
            String fileName = "trade_records_" + System.currentTimeMillis() + ".xlsx";
            // 直接在项目根目录下生成文件
            String filePath = fileName;
            
            tradeRecordService.generateTestExcel(filePath);
            return ResponseEntity.ok("测试Excel文件已生成：" + filePath);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("生成失败：" + e.getMessage());
        }
    }

    @Operation(summary = "查询单条记录", description = "根据ID查询交易记录")
    @GetMapping("/{id}")
    public ResponseEntity<TradeRecord> getById(@PathVariable Long id) {
        return ResponseEntity.ok(tradeRecordService.getById(id));
    }

    @Operation(summary = "新增记录", description = "新增交易记录")
    @PostMapping
    public ResponseEntity<String> add(@RequestBody TradeRecord record) {
        tradeRecordService.insert(record);
        return ResponseEntity.ok("添加成功");
    }

    @Operation(summary = "修改记录", description = "修改交易记录")
    @PutMapping
    public ResponseEntity<String> update(@RequestBody TradeRecord record) {
        tradeRecordService.update(record);
        return ResponseEntity.ok("修改成功");
    }

    @Operation(summary = "删除记录", description = "根据ID删除交易记录")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        tradeRecordService.deleteById(id);
        return ResponseEntity.ok("删除成功");
    }

    @Operation(summary = "批量删除", description = "批量删除交易记录")
    @DeleteMapping("/batch")
    public ResponseEntity<String> batchDelete(@RequestBody List<Long> ids) {
        tradeRecordService.deleteByIds(ids);
        return ResponseEntity.ok("删除成功");
    }

    @Operation(summary = "分页查询", description = "条件分页查询交易记录")
    @PostMapping("/page")
    public ResponseEntity<Map<String, Object>> queryPage(@RequestBody TradeRecordQuery query) {
        return ResponseEntity.ok(tradeRecordService.queryPage(query));
    }
} 