package moe.vitamin.campuslink.command.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moe.vitamin.campuslink.CampusLink;
import moe.vitamin.campuslink.command.api.SlashCommandSource;
import moe.vitamin.campuslink.service.search.ClassSearchManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.io.File;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
public class ClassImportSlashCommand implements SlashCommandSource {

    private final ClassSearchManager classSearchManager;

    @Override
    public SlashCommandData buildCommand() {
        return Commands.slash("과목등록", getDescription())
                .addOption(OptionType.ATTACHMENT, "파일", "업로드할 CSV 파일을 첨부해주세요.", true);
    }

    @Override
    public void onTriggered(SlashCommandInteractionEvent event) {
        OptionMapping fileOption = event.getOption("파일");
        if (fileOption == null) {
            event.reply("CSV 파일을 첨부해 주세요.").setEphemeral(true).queue();
            return;
        }

        Message.Attachment attachment = fileOption.getAsAttachment();
        if (!attachment.getFileName().toLowerCase().endsWith(".csv")) {
            MessageEmbed invalidFileEmbed = new EmbedBuilder()
                    .setTitle("❌ 잘못된 파일 형식")
                    .setDescription("CSV(.csv) 파일만 업로드할 수 있습니다.")
                    .setColor(Color.RED)
                    .setFooter(CampusLink.VERSION)
                    .setTimestamp(LocalDateTime.now())
                    .build();
            event.replyEmbeds(invalidFileEmbed).setEphemeral(true).queue();
            return;
        }

        // Defer reply for async processing (since downloading & parsing file takes time)
        event.deferReply().queue(interactionHook -> {
            File dataFolder = CampusLink.getDataFolder();
            if (dataFolder == null) {
                MessageEmbed errorEmbed = new EmbedBuilder()
                        .setTitle("🚨 폴더 경로 오류")
                        .setDescription("서버 데이터 폴더를 찾을 수 없습니다.")
                        .setColor(Color.RED)
                        .setFooter(CampusLink.VERSION)
                        .setTimestamp(LocalDateTime.now())
                        .build();
                interactionHook.editOriginalEmbeds(errorEmbed).queue();
                return;
            }

            // Create temporary file inside the data folder
            File tempFile = new File(dataFolder, "temp_" + System.currentTimeMillis() + "_" + attachment.getFileName());

            // Defer download
            attachment.getProxy().downloadToFile(tempFile).thenAccept(file -> {
                CompletableFuture.runAsync(() -> {
                    try {
                        long start = System.currentTimeMillis();
                        int importedCount = ClassSearchManager.importCsv(file);
                        long duration = System.currentTimeMillis() - start;

                        if (importedCount > 0) {
                            MessageEmbed successEmbed = new EmbedBuilder()
                                    .setTitle("✅ 과목 등록 완료")
                                    .setDescription("성공적으로 **" + importedCount + "개**의 과목 데이터를 DB에 등록하였습니다!")
                                    .setColor(Color.decode("#0ee111"))
                                    .setFooter(CampusLink.VERSION + " | 소요 시간: " + duration + "ms")
                                    .setTimestamp(LocalDateTime.now())
                                    .build();
                            interactionHook.editOriginalEmbeds(successEmbed).queue();
                        } else {
                            MessageEmbed failureEmbed = new EmbedBuilder()
                                    .setTitle("❌ 과목 등록 실패")
                                    .setDescription("CSV 파일에서 읽어들인 데이터가 없거나 파일 형식이 유효하지 않습니다.")
                                    .setColor(Color.RED)
                                    .setFooter(CampusLink.VERSION)
                                    .setTimestamp(LocalDateTime.now())
                                    .build();
                            interactionHook.editOriginalEmbeds(failureEmbed).queue();
                        }
                    } catch (Exception e) {
                        log.error("Failed to parse and import CSV file: {}", file.getName(), e);
                        MessageEmbed errorEmbed = new EmbedBuilder()
                                .setTitle("🚨 등록 오류 발생")
                                .setDescription("CSV 파일을 처리하는 중 예상치 못한 서버 내부 오류가 발생했습니다.")
                                .setColor(Color.RED)
                                .setFooter(CampusLink.VERSION)
                                .setTimestamp(LocalDateTime.now())
                                .build();
                        interactionHook.editOriginalEmbeds(errorEmbed).queue();
                    } finally {
                        if (file.exists()) {
                            if (!file.delete()) {
                                log.warn("Failed to delete temporary CSV file: {}", file.getAbsolutePath());
                            }
                        }
                    }
                });
            }).exceptionally(e -> {
                log.error("Failed to download CSV attachment: {}", attachment.getFileName(), e);
                MessageEmbed downloadErrorEmbed = new EmbedBuilder()
                        .setTitle("🚨 다운로드 실패")
                        .setDescription("첨부 파일을 다운로드하는 도중 오류가 발생했습니다.")
                        .setColor(Color.RED)
                        .setFooter(CampusLink.VERSION)
                        .setTimestamp(LocalDateTime.now())
                        .build();
                interactionHook.editOriginalEmbeds(downloadErrorEmbed).queue();
                
                // Cleanup temp file if download failed half-way
                if (tempFile.exists()) {
                    tempFile.delete();
                }
                return null;
            });
        });
    }

    @NotNull
    @Override
    public String getDescription() {
        return "CSV 파일을 업로드하여 과목 데이터베이스에 등록합니다.";
    }

    @Nullable
    @Override
    public String getUsage() {
        return "/과목등록 파일:[CSV 파일 첨부]";
    }
}
