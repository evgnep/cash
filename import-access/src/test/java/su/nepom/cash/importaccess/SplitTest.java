package su.nepom.cash.importaccess;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class SplitTest {
    @Test
    @DisplayName("Разбиение без кавычек")
    void Should_Split_When_NoQuotes() {
        assertThat(Split.split("1;2;3 42")).containsExactly("1", "2", "3 42");
        assertThat(Split.split("1;;3")).containsExactly("1", "", "3");
        assertThat(Split.split(";;3")).containsExactly("", "", "3");
        assertThat(Split.split("")).containsExactly("");
        assertThat(Split.split(";")).containsExactly("", "");
    }

    @Test
    @DisplayName("Разбиение с кавычками")
    void Should_Split_When_Quotes() {
        assertThat(Split.split("\"1\";\"hello; world\";3 42")).containsExactly("1", "hello; world", "3 42");
        assertThat(Split.split("\"q\";\"w\";\"e\"")).containsExactly("q", "w", "e");
        assertThat(Split.split("\"q\";w;\"e\"")).containsExactly("q", "w", "e");
    }

    @Test
    @DisplayName("Разбиение с экранируемыми кавычками")
    void Should_Split_When_EscapedQuotes() {
        assertThat(Split.split("\"xx; \"\"YYY;\"\" z\";5")).containsExactly("xx; \"YYY;\" z", "5");
        assertThat(Split.split("\"\"\"Q\"\"\"")).containsExactly("\"Q\"");
    }

    @Test
    @DisplayName("Разбиение с преобразованием в целое")
    void Should_ConvertToInt() {
        assertThat(Split.splitAndConvert("5;62", "ii")).containsExactly(5, 62);
    }

    @Test
    @DisplayName("Разбиение с преобразованием в строку")
    void Should_ConvertToString() {
        assertThat(Split.splitAndConvert("5;\"62\"", "ss")).containsExactly("5", "62");
    }

    @Test
    @DisplayName("Разбиение с преобразованием в дату")
    void Should_ConvertToDate() {
        assertThat(Split.splitAndConvert("3.2.2018 20:25:10", "d")).containsExactly(Instant.parse("2018-02-03T17:25:10Z"));
        assertThat(Split.splitAndConvert("31.12.2018 7:25:10", "d")).containsExactly(Instant.parse("2018-12-31T04:25:10Z"));
    }

    @Test
    @DisplayName("Разбиение с преобразованием в деньги")
    void Should_ConvertToBigDecimal() {
        assertThat(Split.splitAndConvert("-500.00 r;42.54 r", "mm")).containsExactly(new BigDecimal("-500.00"), new BigDecimal("42.54"));
    }

}

