package moe.vitamin.campuslink.command.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moe.vitamin.campuslink.CampusLink;
import moe.vitamin.campuslink.command.api.SlashCommandSource;
import moe.vitamin.campuslink.service.search.ClassSearchManager;
import moe.vitamin.campuslink.service.search.ClassDataDto;
import moe.vitamin.campuslink.service.search.database.ClassSearchDao;
import moe.vitamin.campuslink.service.certification.api.EmailCertificationAPI;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
public class ClassSearchSlashCommand implements SlashCommandSource {

    private final ClassSearchManager classSearchManager;

    @Override
    public SlashCommandData buildCommand() {
        return Commands.slash("과목검색", getDescription())
                .addOptions(
                        new OptionData(OptionType.STRING, "방법", "검색 방법을 선택하세요.", true)
                                .addChoice("수강번호", "번호")
                                .addChoice("교과목명", "과목")
                                .addChoice("담당교수", "교수")
                                .addChoice("수강학과", "학과")
                                .addChoice("이수구분", "구분"),
                        new OptionData(OptionType.STRING, "검색어", "검색할 검색어를 입력하세요.", true)
                );
    }

    @Override
    public void onTriggered(SlashCommandInteractionEvent event) {
        var guild = event.getGuild();
        if (guild == null) {
            event.reply("DM 채널에서는 사용할 수 없는 명령어입니다.").setEphemeral(true).queue();
            return;
        }

        String method = event.getOption("방법") != null ? event.getOption("방법").getAsString() : null;
        String query = event.getOption("검색어") != null ? event.getOption("검색어").getAsString() : null;

        if (method == null || query == null || query.trim().isEmpty()) {
            event.reply("올바른 검색방법과 검색어를 입력해주세요.").setEphemeral(true).queue();
            return;
        }

        final String searchMethod = method.trim();
        final String searchQuery = query.trim();

        // Check if the user is email certified first
        EmailCertificationAPI.isEmailCertified(event.getUser().getIdLong(), guild.getIdLong())
                .thenAccept(certified -> {
                    if (!certified) {
                        MessageEmbed notCertifiedEmbed = new EmbedBuilder()
                                .setTitle("❌ 학교 인증 필요")
                                .setDescription("과목 검색 기능은 이메일 인증된 사람만 사용할 수 있습니다.\n`/인증` 명령어를 통해 먼저 학교 이메일 인증을 완료해주세요.")
                                .setColor(Color.RED)
                                .setFooter(CampusLink.VERSION)
                                .setTimestamp(LocalDateTime.now())
                                .build();
                        event.replyEmbeds(notCertifiedEmbed).setEphemeral(true).queue();
                        return;
                    }

                    // Defer reply for async processing
                    event.deferReply().queue(interactionHook -> {
                        long start = System.currentTimeMillis();
                        CompletableFuture.runAsync(() -> {
                            try {
                                List<ClassDataDto> results = new ArrayList<>();

                                switch (searchMethod) {
                                    case "번호" -> {
                                        ClassDataDto dto = ClassSearchDao.findByCourseNumber(searchQuery);
                                        if (dto != null) {
                                            results.add(dto);
                                        }
                                    }
                                    case "과목" -> 
                                        results = ClassSearchDao.searchClasses(null, null, null, searchQuery, null, null, null);
                                    case "교수" -> 
                                        results = ClassSearchDao.searchClasses(null, null, null, null, null, searchQuery, null);
                                    case "학과" -> 
                                        results = ClassSearchDao.searchClasses(null, searchQuery, null, null, null, null, null);
                                    case "구분" -> 
                                        results = ClassSearchDao.searchClasses(searchQuery, null, null, null, null, null, null);
                                    default -> {
                                        MessageEmbed invalidMethodEmbed = new EmbedBuilder()
                                                .setTitle("⚠️ 올바르지 않은 검색방법")
                                                .setDescription("올바른 검색방법을 선택해주세요.")
                                                .setColor(Color.RED)
                                                .setFooter(CampusLink.VERSION)
                                                .setTimestamp(LocalDateTime.now())
                                                .build();
                                        interactionHook.editOriginalEmbeds(invalidMethodEmbed).queue();
                                        return;
                                    }
                                }

                                long duration = System.currentTimeMillis() - start;

                                if (results.isEmpty()) {
                                    MessageEmbed noResultEmbed = new EmbedBuilder()
                                            .setTitle("❌ 검색 결과가 없습니다.")
                                            .setDescription("'" + searchQuery + "'에 해당하는 과목 정보를 찾을 수 없습니다. (소요 시간: " + duration + "ms)")
                                            .setColor(Color.RED)
                                            .setFooter(CampusLink.VERSION)
                                            .setTimestamp(LocalDateTime.now())
                                            .build();
                                    interactionHook.editOriginalEmbeds(noResultEmbed).queue();
                                } else if (results.size() == 1) {
                                    ClassDataDto classData = results.get(0);
                                    EmbedBuilder embedBuilder = new EmbedBuilder()
                                            .setTitle("📚 [" + classData.getCourseNumber() + "] " + classData.getCourseName())
                                            .setColor(Color.decode("#9047ff"))
                                            .addField("담당교수", classData.getProfessor() != null && !classData.getProfessor().isEmpty() ? classData.getProfessor() : "없음", true)
                                            .addField("학년 / 학점", classData.getGrade() + "학년 / " + classData.getCredits() + "학점", true)
                                            .addField("이수구분", classData.getClassification() != null && !classData.getClassification().isEmpty() ? classData.getClassification() : "없음", true)
                                            .addField("강의시간", (classData.getDayOfWeek() != null && !classData.getDayOfWeek().isEmpty() ? classData.getDayOfWeek() + "요일 " : "")
                                                    + (classData.getStartTime() != null ? classData.getStartTime() + " ~ " + classData.getEndTime() : "정보 없음"), false)
                                            .addField("강의실", classData.getClassroom() != null && !classData.getClassroom().isEmpty() ? classData.getClassroom() : "없음", true)
                                            .addField("비고", classData.getRemarks() != null && !classData.getRemarks().isEmpty() ? classData.getRemarks() : "없음", false)
                                            .setFooter(CampusLink.VERSION + " | 소요 시간: " + duration + "ms")
                                            .setTimestamp(LocalDateTime.now());
                                    interactionHook.editOriginalEmbeds(embedBuilder.build()).queue();
                                } else {
                                    EmbedBuilder embedBuilder = new EmbedBuilder()
                                            .setTitle("🔍 과목 검색 결과 (총 " + results.size() + "건)")
                                            .setColor(Color.decode("#9047ff"))
                                            .setFooter(CampusLink.VERSION + " | 소요 시간: " + duration + "ms")
                                            .setTimestamp(LocalDateTime.now());

                                    StringBuilder sb = new StringBuilder();
                                    int limit = Math.min(results.size(), 10);
                                    for (int i = 0; i < limit; i++) {
                                        ClassDataDto classData = results.get(i);
                                        sb.append("• **").append(classData.getCourseNumber()).append("** | ")
                                                .append(classData.getCourseName()).append(" (")
                                                .append(classData.getProfessor() != null && !classData.getProfessor().isEmpty() ? classData.getProfessor() : "담당 미지정")
                                                .append(")\n")
                                                .append("  └ 구분: ").append(classData.getClassification() != null ? classData.getClassification() : "없음")
                                                .append(" | ").append(classData.getCredits()).append("학점")
                                                .append(classData.getDayOfWeek() != null ? " | " + classData.getDayOfWeek() + "요일" : "")
                                                .append(classData.getStartTime() != null ? " " + classData.getStartTime() + "~" + classData.getEndTime() : "")
                                                .append(classData.getClassroom() != null && !classData.getClassroom().isEmpty() ? " | " + classData.getClassroom() : "")
                                                .append("\n\n");
                                    }
                                    if (results.size() > 10) {
                                        sb.append("*검색 결과가 너무 많아 상위 10건만 표시합니다. 더 구체적인 검색어로 검색해 주세요.*");
                                    }
                                    embedBuilder.setDescription(sb.toString());
                                    interactionHook.editOriginalEmbeds(embedBuilder.build()).queue();
                                }
                            } catch (Exception e) {
                                log.error("Failed to query course via slash command: method={}, query={}", searchMethod, searchQuery, e);
                                MessageEmbed errorEmbed = new EmbedBuilder()
                                        .setTitle("🚨 조회 오류 발생")
                                        .setDescription("과목을 조회하는 도중 예상치 못한 서버 내부 오류가 발생했습니다.")
                                        .setColor(Color.RED)
                                        .setFooter(CampusLink.VERSION)
                                        .setTimestamp(LocalDateTime.now())
                                        .build();
                                interactionHook.editOriginalEmbeds(errorEmbed).queue();
                            }
                        });
                    });
                }).exceptionally(e -> {
                    log.error("Failed to check if user {} is certified", event.getUser().getIdLong(), e);
                    event.reply("오류가 발생했습니다. 잠시 후 다시 시도해주세요.").setEphemeral(true).queue();
                    return null;
                });
    }

    @NotNull
    @Override
    public String getDescription() {
        return "선택한 방법으로 과목 정보를 조회합니다.";
    }

    @Nullable
    @Override
    public String getUsage() {
        return "/과목검색 방법:[검색방법] 검색어:[검색어]";
    }
}
