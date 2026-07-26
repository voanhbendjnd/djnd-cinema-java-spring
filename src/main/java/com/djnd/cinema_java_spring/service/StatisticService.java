package com.djnd.cinema_java_spring.service;

import com.djnd.cinema_java_spring.repository.BookingRepository;
import com.djnd.cinema_java_spring.repository.MovieRepository;
import com.djnd.cinema_java_spring.service.dto.OccupancyReportDTO;
import com.djnd.cinema_java_spring.service.dto.ResStatisticMetricDTO;
import com.djnd.cinema_java_spring.service.projection.OccupancyProjection;
import com.djnd.cinema_java_spring.service.projection.SalesChartProjection;
import com.djnd.cinema_java_spring.service.projection.TodayMetricsProjection;
import com.djnd.cinema_java_spring.service.projection.OccupancyMovieDetailProjection;
import io.jsonwebtoken.io.IOException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Service
public class StatisticService {
    final BookingRepository bookingRepository;
    final MovieRepository movieRepository;
    // 7 days and metric today
    public ResStatisticMetricDTO getSalesChartMetricsPublish(LocalDate fromDate, LocalDate toDate,Integer roomId) {
        TodayMetricsProjection todayMetrics = bookingRepository.getTodayRevenue();
        LocalDateTime fromDateTime = fromDate.atStartOfDay();
        LocalDateTime toDateTime = toDate.atStartOfDay().plusDays(1);
        List<SalesChartProjection> chartData = bookingRepository.getSalesChartMetrics(fromDateTime, toDateTime, roomId);
        ResStatisticMetricDTO res = new ResStatisticMetricDTO();
        res.setChartData(chartData);
        res.setTodayMetrics(todayMetrics);
        return res;
    }

    /*
    * Get top movie
    * */
    public List<OccupancyMovieDetailProjection> getTopMovieStatistics(LocalDate fromDate, LocalDate toDate, int limit) {
        return movieRepository.getTopPerformingMovies(fromDate.atStartOfDay(), toDate.atStartOfDay().plusDays(1), limit);
    }
    public OccupancyReportDTO getReportOccupancy(LocalDate fromDate, LocalDate toDate, int limit) {
        OccupancyReportDTO res = new OccupancyReportDTO();
        res.setSummary(bookingRepository.getStatisticReportOccupancy(fromDate.atStartOfDay(), toDate.plusDays(1).atStartOfDay()).orElse(null));
        res.setDetails(this.getTopMovieStatistics(fromDate, toDate, limit));
        return res;
    }

    public byte[] exportToExcel(LocalDate fromDate, LocalDate toDate, int limit) throws IOException {
        OccupancyProjection occupancyProjection = bookingRepository.getStatisticReportOccupancy(fromDate.atStartOfDay(), toDate.atStartOfDay().plusDays(1)).orElse(null);
        List<OccupancyMovieDetailProjection> occupancyMovieDetailProjections = this.getTopMovieStatistics(fromDate, toDate, limit);
        try(Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Occupancy rate");
            Row headerRow = sheet.createRow(0);
            String [] headers = {"No", "Movie title",  "Tickets sold", "Occupancy rate"};
            for(int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);

            }
            CellStyle style = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            style.setDataFormat(format.getFormat("0.00"));
            int totalTickets = 0;
            int totalCapacity = 0;
            int rowIdx = 1;
            for(OccupancyMovieDetailProjection detail : occupancyMovieDetailProjections) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(rowIdx - 1);
                row.createCell(1).setCellValue(detail.getMovieTitle());
                row.createCell(2).setCellValue(detail.getTicketsSold());
                row.createCell(3).setCellValue(detail.getOccupancyRate() + "%");

            }
            Row totalRow = sheet.createRow(rowIdx);
            CellStyle boldStyle =  workbook.createCellStyle();
            Font boldFont = workbook.createFont();
            boldFont.setBold(true);
            boldStyle.setFont(boldFont);
            Cell totalLabelCell = totalRow.createCell(2);
            totalLabelCell.setCellStyle(boldStyle);
            totalLabelCell.setCellValue("Summary");
            Cell totalCapCell = totalRow.createCell(2);
            assert occupancyProjection != null;
            totalLabelCell.setCellValue(occupancyProjection.getTotalTicketsSold() != null ? occupancyProjection.getTotalTicketsSold().toString() : "0");
            totalCapCell.setCellStyle(boldStyle);
            Cell overallRateCell = totalRow.createCell(3);
            if(occupancyProjection.getTotalTicketsSold() != null) {
                overallRateCell.setCellValue(occupancyProjection.getTotalTicketsSold().toString());
            }
            else{
                overallRateCell.setCellValue("0");
            }
            overallRateCell.setCellStyle(boldStyle);
            for(int i = 0; i< headers.length; i++){
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return out.toByteArray();
        }
        catch(Exception e){
            throw new IOException("Cannot export!");
        }
    }
}
